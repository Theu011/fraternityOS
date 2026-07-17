package com.fraternityos.server.responsibility.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * A chore or duty. For ROTATING responsibilities {@code rotationDays} sets the
 * cadence and {@code rotationCursor} points at the last-assigned member in the
 * ordered pool ({@link ResponsibilityMember}).
 */
@Entity
@Table(name = "responsibility")
public class Responsibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "house_id", nullable = false)
    private Long houseId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResponsibilityType type;

    @Column(name = "rotation_days")
    private Integer rotationDays;

    @Column(name = "rotation_cursor")
    private Integer rotationCursor;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Responsibility() {
        // for JPA
    }

    public Responsibility(Long houseId, String title, ResponsibilityType type) {
        this.houseId = houseId;
        this.title = title;
        this.type = type;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getHouseId() {
        return houseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ResponsibilityType getType() {
        return type;
    }

    public void setType(ResponsibilityType type) {
        this.type = type;
    }

    public Integer getRotationDays() {
        return rotationDays;
    }

    public void setRotationDays(Integer rotationDays) {
        this.rotationDays = rotationDays;
    }

    public Integer getRotationCursor() {
        return rotationCursor;
    }

    public void setRotationCursor(Integer rotationCursor) {
        this.rotationCursor = rotationCursor;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
