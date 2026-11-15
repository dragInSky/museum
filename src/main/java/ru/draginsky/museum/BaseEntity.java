package ru.draginsky.museum;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.proxy.HibernateProxy;

@MappedSuperclass
@Getter
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "museum_seq")
    @SequenceGenerator(name = "museum_seq", sequenceName = "museum_seq", allocationSize = 1)
    private Long id;

    @Override
    public final boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BaseEntity entity)) return false;
        return entityType() == entity.entityType() && getId() != null && getId().equals(entity.getId());
    }

    @Override
    public final int hashCode() {
        return entityType().hashCode();
    }

    private Class<?> entityType() {
        return this instanceof HibernateProxy proxy
            ? proxy.getHibernateLazyInitializer().getPersistentClass() : getClass();
    }
}
