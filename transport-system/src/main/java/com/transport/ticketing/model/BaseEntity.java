package com.transport.ticketing.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Simple base entity with id and creation time.
 */
public abstract class BaseEntity {
    private final String id;
    private final Instant createdAt;

    protected BaseEntity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
