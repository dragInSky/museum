package ru.draginsky.museum;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hall extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "hall", fetch = FetchType.LAZY)
    private List<Exhibit> exhibits = new ArrayList<>();

    public Hall(String name) {
        this.name = name;
    }

    public void addExhibit(Exhibit exhibit) {
        if (exhibit.getHall() == this) return;
        if (exhibit.getHall() != null) exhibit.getHall().getExhibits().remove(exhibit);
        exhibits.add(exhibit);
        exhibit.setHall(this);
    }
}
