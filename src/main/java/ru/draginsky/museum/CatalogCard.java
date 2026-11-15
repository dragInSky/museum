package ru.draginsky.museum;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatalogCard extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String number;

    public CatalogCard(String number) {
        this.number = number;
    }

    public void changeNumber(String number) {
        this.number = number;
    }
}
