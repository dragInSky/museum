package ru.draginsky.museum;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "demo.enabled=false")
class MuseumTest {
    @Autowired
    private MuseumService service;

    @Autowired
    private EntityManagerFactory factory;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void seed() {
        service.seed();
    }

    @Test
    void relations() {
        assertThat(service.halls()).containsExactly(
            "Археология: [Амфора, Монета]", "Живопись: [Пейзаж]"
        );
        assertThat(service.routes()).containsExactly(
            "Обзорная: 1. Амфора", "Обзорная: 2. Пейзаж",
            "История: 1. Амфора", "История: 2. Монета"
        );
        assertThat(service.search(null, null, null)).extracting(MuseumService.ExhibitView::card)
            .containsExactly("M-001", "M-002", "M-003");
    }

    @Test
    void repositoryQueries() {
        assertThat(service.byName("МОН")).extracting(MuseumService.ExhibitView::name)
            .containsExactly("Монета");
        assertThat(service.byHall("Археология")).extracting(MuseumService.ExhibitView::name)
            .containsExactly("Амфора", "Монета");
        assertThat(service.since(1600)).extracting(MuseumService.ExhibitView::name)
            .containsExactly("Монета", "Пейзаж");
        assertThat(service.since(2000)).isEmpty();
    }

    @Test
    void optionalFilters() {
        assertThat(service.search(null, null, null)).hasSize(3);
        assertThat(service.search(" ", "", null)).hasSize(3);
        assertThat(service.search(" МОН ", null, null)).extracting(MuseumService.ExhibitView::name)
            .containsExactly("Монета");
        assertThat(service.search(null, "Живопись", null)).extracting(MuseumService.ExhibitView::name)
            .containsExactly("Пейзаж");
        assertThat(service.search(null, null, 1600)).hasSize(2);
        assertThat(service.search("мон", "Археология", 1600)).hasSize(1);
        assertThat(service.search("мон", "Живопись", 1600)).isEmpty();
        assertThat(service.search(null, null, 2000)).isEmpty();
    }

    @Test
    void repeatedSeed() {
        service.seed();
        assertThat(service.search(null, null, null)).hasSize(3);
        assertThat(service.routes()).hasSize(4);
    }

    @Test
    void mappingFeatures() {
        assertThat(service.mappingCheck()).isEqualTo(
            new MuseumService.MappingCheck(true, true, true, true, true)
        );
        assertThat(service.halls()).hasSize(2);
        assertThat(service.search(null, null, null)).hasSize(3);
    }

    @Test
    void movingExhibit() {
        var first = new Hall("Первый");
        var second = new Hall("Второй");
        var exhibit = new Exhibit("Экспонат", 2000, new CatalogCard("MOVE"));
        first.addExhibit(exhibit);
        second.addExhibit(exhibit);
        second.addExhibit(exhibit);
        assertThat(first.getExhibits()).isEmpty();
        assertThat(second.getExhibits()).containsExactly(exhibit);
        assertThat(exhibit.getHall()).isSameAs(second);
        assertThat(exhibit).isNotEqualTo(new Exhibit("Экспонат", 2000, new CatalogCard("MOVE")));
    }

    @Test
    void databaseSchema() {
        assertThat(jdbc.queryForObject("""
            select count(*) from information_schema.columns
            where table_schema = 'public' and column_name = 'id' and is_identity = 'YES'
            """, Integer.class)).isZero();
        assertThat(jdbc.queryForObject(
            "select increment_by from pg_sequences where schemaname = 'public' and sequencename = 'museum_seq'",
            Long.class)).isEqualTo(1L);
        assertThat(jdbc.queryForObject(
            "select count(*) from flyway_schema_history where version = '1' and success",
            Integer.class)).isEqualTo(1);
    }

    @Test
    void optimisticLock() {
        var first = factory.createEntityManager();
        var second = factory.createEntityManager();
        Long id = null;
        try {
            first.getTransaction().begin();
            var hall = first.createQuery("from Hall", Hall.class).setMaxResults(1).getSingleResult();
            var exhibit = new Exhibit("Проверка версии", 2000, new CatalogCard(UUID.randomUUID().toString()));
            hall.addExhibit(exhibit);
            first.persist(exhibit);
            first.getTransaction().commit();
            id = exhibit.getId();
            first.getTransaction().begin();
            second.getTransaction().begin();
            var stale = second.find(Exhibit.class, id);
            exhibit.changeYear(2001);
            first.getTransaction().commit();
            stale.changeYear(2002);
            assertThatThrownBy(() -> second.getTransaction().commit())
                .isInstanceOf(RollbackException.class).hasCauseInstanceOf(OptimisticLockException.class);
        } finally {
            if (second.getTransaction().isActive()) second.getTransaction().rollback();
            if (first.getTransaction().isActive()) first.getTransaction().rollback();
            second.close();
            first.close();
            if (id != null) {
                var cleanup = factory.createEntityManager();
                try {
                    cleanup.getTransaction().begin();
                    cleanup.remove(cleanup.find(Exhibit.class, id));
                    cleanup.getTransaction().commit();
                } finally {
                    cleanup.close();
                }
            }
        }
    }
}
