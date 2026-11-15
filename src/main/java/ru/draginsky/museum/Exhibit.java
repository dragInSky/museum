package ru.draginsky.museum;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exhibit extends BaseEntity {
    @Version
    private Long version;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int year;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @OneToOne(fetch = FetchType.LAZY, optional = false,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @JoinColumn(name = "card_id", nullable = false, unique = true)
    private CatalogCard card;

    public Exhibit(String name, int year, CatalogCard card) {
        this.name = name;
        this.year = year;
        this.card = card;
    }

    void setHall(Hall hall) {
        this.hall = hall;
    }

    public void replaceCard(CatalogCard card) {
        this.card = card;
    }

    public void changeYear(int year) {
        this.year = year;
    }
}
