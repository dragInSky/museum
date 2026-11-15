package ru.draginsky.museum;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TourStopRepository extends JpaRepository<TourStop, Long> {
    @EntityGraph(attributePaths = {"tour", "exhibit"})
    List<TourStop> findAllByOrderByTourIdAscPositionAsc();
}
