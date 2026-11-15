package ru.draginsky.museum;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MuseumService {
    private final HallRepository halls;
    private final ExhibitRepository exhibits;
    private final CatalogCardRepository cards;
    private final TourRepository tours;
    private final TourStopRepository stops;
    private final EntityManager em;

    public record ExhibitView(String name, int year, String hall, String card) {
    }

    public record MappingCheck(boolean cascade, boolean orphanRemoval, boolean helper,
                               boolean equality, boolean version) {
    }

    @Transactional
    public void seed() {
        if (halls.count() > 0) return;
        var archaeology = halls.save(new Hall("Археология"));
        var painting = halls.save(new Hall("Живопись"));
        var amphora = new Exhibit("Амфора", 500, new CatalogCard("M-001"));
        var coin = new Exhibit("Монета", 1600, new CatalogCard("M-002"));
        var landscape = new Exhibit("Пейзаж", 1890, new CatalogCard("M-003"));
        archaeology.addExhibit(amphora);
        archaeology.addExhibit(coin);
        painting.addExhibit(landscape);
        exhibits.saveAll(List.of(amphora, coin, landscape));
        var overview = tours.save(new Tour("Обзорная"));
        var history = tours.save(new Tour("История"));
        stops.saveAll(List.of(
            new TourStop(overview, amphora, 1),
            new TourStop(overview, landscape, 2),
            new TourStop(history, amphora, 1),
            new TourStop(history, coin, 2)
        ));
    }

    @Transactional(readOnly = true)
    public List<String> halls() {
        return halls.findAllByOrderByIdAsc().stream()
            .map(h -> h.getName() + ": " + h.getExhibits().stream()
                .map(Exhibit::getName).sorted().toList())
            .toList();
    }

    @Transactional(readOnly = true)
    public List<String> routes() {
        return stops.findAllByOrderByTourIdAscPositionAsc().stream()
            .map(s -> s.getTour().getName() + ": " + s.getPosition() + ". " + s.getExhibit().getName())
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ExhibitView> byName(String name) {
        return views(exhibits.findByNameContainingIgnoreCaseOrderById(name));
    }

    @Transactional(readOnly = true)
    public List<ExhibitView> byHall(String hall) {
        return views(exhibits.findByHall(hall));
    }

    @Transactional(readOnly = true)
    public List<ExhibitView> since(int year) {
        return views(exhibits.findByIdInOrderByIdAsc(exhibits.findSince(year)));
    }

    @Transactional(readOnly = true)
    public List<ExhibitView> search(String name, String hall, Integer yearFrom) {
        var cb = em.getCriteriaBuilder();
        var query = cb.createQuery(Exhibit.class);
        Root<Exhibit> root = query.from(Exhibit.class);
        root.fetch("hall");
        root.fetch("card");
        var filters = new ArrayList<Predicate>();
        if (name != null && !name.isBlank()) {
            filters.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase(Locale.ROOT) + "%"));
        }
        if (hall != null && !hall.isBlank()) {
            filters.add(cb.equal(root.get("hall").get("name"), hall.trim()));
        }
        if (yearFrom != null) {
            filters.add(cb.greaterThanOrEqualTo(root.get("year"), yearFrom));
        }
        query.select(root).where(filters.toArray(Predicate[]::new)).orderBy(cb.asc(root.get("id")));
        return views(em.createQuery(query).getResultList());
    }

    @Transactional
    public MappingCheck mappingCheck() {
        var hall = halls.save(new Hall("Проверка"));
        var exhibit = new Exhibit("Проверка", 2000, new CatalogCard("CHECK-OLD"));
        int hash = exhibit.hashCode();
        hall.addExhibit(exhibit);
        exhibits.saveAndFlush(exhibit);
        boolean helper = exhibit.getHall() == hall && hall.getExhibits().contains(exhibit);
        Long oldCard = exhibit.getCard().getId();
        boolean cascade = cards.existsById(oldCard);
        long oldVersion = exhibit.getVersion();
        exhibit.replaceCard(new CatalogCard("CHECK-NEW"));
        exhibit.changeYear(2001);
        em.flush();
        Long newCard = exhibit.getCard().getId();
        boolean orphan = !cards.existsById(oldCard) && cards.existsById(newCard);
        boolean version = exhibit.getVersion() > oldVersion;
        em.detach(exhibit);
        em.detach(exhibit.getCard());
        exhibit.getCard().changeNumber("CHECK-MERGED");
        var merged = exhibits.saveAndFlush(exhibit);
        em.refresh(merged.getCard());
        cascade = cascade && merged.getCard().getNumber().equals("CHECK-MERGED");
        em.detach(merged);
        var proxy = exhibits.getReferenceById(exhibit.getId());
        boolean equality = exhibit.equals(proxy) && proxy.equals(exhibit)
            && hash == proxy.hashCode() && !Hibernate.isInitialized(proxy);
        exhibits.delete(proxy);
        em.flush();
        orphan = orphan && !cards.existsById(newCard);
        halls.delete(hall);
        return new MappingCheck(cascade, orphan, helper, equality, version);
    }

    private List<ExhibitView> views(List<Exhibit> items) {
        return items.stream().map(e -> new ExhibitView(
            e.getName(), e.getYear(), e.getHall().getName(), e.getCard().getNumber()
        )).toList();
    }
}
