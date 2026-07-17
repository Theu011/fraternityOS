package com.fraternityos.server.house.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

/** Join row assigning a {@link Position} to a {@link Membership} (many-to-many). */
@Entity
@Table(name = "membership_position")
@IdClass(MembershipPositionId.class)
public class MembershipPosition {

    @Id
    @Column(name = "membership_id")
    private Long membershipId;

    @Id
    @Column(name = "position_id")
    private Long positionId;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    protected MembershipPosition() {
        // for JPA
    }

    public MembershipPosition(Long membershipId, Long positionId) {
        this.membershipId = membershipId;
        this.positionId = positionId;
    }

    @PrePersist
    void onCreate() {
        this.assignedAt = Instant.now();
    }

    public Long getMembershipId() {
        return membershipId;
    }

    public Long getPositionId() {
        return positionId;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }
}
