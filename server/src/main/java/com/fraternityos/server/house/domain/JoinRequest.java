package com.fraternityos.server.house.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * A user's request to join an existing house. Created PENDING; a President later
 * approves or rejects it. Referenced by {@code userId} (not a membership) because
 * the requester has no membership in the target house yet.
 */
@Entity
@Table(name = "join_request")
public class JoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "house_id", nullable = false)
    private Long houseId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JoinRequestStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    protected JoinRequest() {
        // for JPA
    }

    public JoinRequest(Long houseId, Long userId) {
        this.houseId = houseId;
        this.userId = userId;
        this.status = JoinRequestStatus.PENDING;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    /** Approve this request (a membership is created separately). */
    public void approve() {
        this.status = JoinRequestStatus.APPROVED;
        this.decidedAt = Instant.now();
    }

    /** Reject this request. */
    public void reject() {
        this.status = JoinRequestStatus.REJECTED;
        this.decidedAt = Instant.now();
    }

    public boolean isPending() {
        return this.status == JoinRequestStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public Long getHouseId() {
        return houseId;
    }

    public Long getUserId() {
        return userId;
    }

    public JoinRequestStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }
}
