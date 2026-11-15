package ru.draginsky.museum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ExhibitRepository extends JpaRepository<Exhibit, Long> {
    @EntityGraph(attributePaths = {"hall", "card"})
    List<Exhibit> findByNameContainingIgnoreCaseOrderById(String name);

    @EntityGraph(attributePaths = {"hall", "card"})
    @Query("select e from Exhibit e where e.hall.name = :hall order by e.id")
    List<Exhibit> findByHall(@Param("hall") String hall);

    @Query(value = "select id from exhibit where year >= :year order by id", nativeQuery = true)
    List<Long> findSince(@Param("year") int year);

    @EntityGraph(attributePaths = {"hall", "card"})
    List<Exhibit> findByIdInOrderByIdAsc(List<Long> ids);
}
