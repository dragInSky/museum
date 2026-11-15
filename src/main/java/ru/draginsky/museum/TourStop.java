package ru.draginsky.museum;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tour_id", "exhibit_id"}),
    @UniqueConstraint(columnNames = {"tour_id", "position"})
})
public class TourStop extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exhibit_id", nullable = false)
    private Exhibit exhibit;

    @Column(nullable = false)
    private int position;

    public TourStop(Tour tour, Exhibit exhibit, int position) {
        this.tour = tour;
        this.exhibit = exhibit;
        this.position = position;
    }
}
