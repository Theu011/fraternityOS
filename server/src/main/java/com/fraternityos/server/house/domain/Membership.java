package com.fraternityos.server.house.domain;

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
 * The relationship between a {@link User} and a {@link House}: the house-specific
 * facet of a person (status, room, join/graduation dates). Every house-scoped
 * domain row references a membership, so this record is the tenancy anchor —
 * {@code houseId} is the multi-tenancy scope key. A user has at most one
 * membership (enforced by a unique constraint on {@code user_id}).
 */
@Entity
@Table(name = "membership")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "house_id", nullable = false)
    private Long houseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    private String room;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "graduated_at")
    private Instant graduatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Membership() {
        // for JPA
    }

    public Membership(Long userId, Long houseId, MemberStatus status) {
        this.userId = userId;
        this.houseId = houseId;
        this.status = status;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.joinedAt == null) {
            this.joinedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getHouseId() {
        return houseId;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public Instant getGraduatedAt() {
        return graduatedAt;
    }

    public void setGraduatedAt(Instant graduatedAt) {
        this.graduatedAt = graduatedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
