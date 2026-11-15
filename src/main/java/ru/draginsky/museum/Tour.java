package ru.draginsky.museum;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tour extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;

    public Tour(String name) {
        this.name = name;
    }
}
