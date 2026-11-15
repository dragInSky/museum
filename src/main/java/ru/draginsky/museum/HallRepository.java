package ru.draginsky.museum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;

public interface HallRepository extends JpaRepository<Hall, Long> {
    @EntityGraph(attributePaths = "exhibits")
    List<Hall> findAllByOrderByIdAsc();
}
