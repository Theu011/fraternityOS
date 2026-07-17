package com.fraternityos.server.responsibility.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * An eligible member in a rotating responsibility's pool. {@code rotationOrder}
 * defines the position the scheduled worker walks to pick the next assignee.
 */
@Entity
@Table(name = "responsibility_member")
@IdClass(ResponsibilityMemberId.class)
public class ResponsibilityMember {

    @Id
    @Column(name = "responsibility_id")
    private Long responsibilityId;

    @Id
    @Column(name = "membership_id")
    private Long membershipId;

    @Column(name = "rotation_order", nullable = false)
    private int rotationOrder;

    protected ResponsibilityMember() {
        // for JPA
    }

    public ResponsibilityMember(Long responsibilityId, Long membershipId, int rotationOrder) {
        this.responsibilityId = responsibilityId;
        this.membershipId = membershipId;
        this.rotationOrder = rotationOrder;
    }

    public Long getResponsibilityId() {
        return responsibilityId;
    }

    public Long getMembershipId() {
        return membershipId;
    }

    public int getRotationOrder() {
        return rotationOrder;
    }

    public void setRotationOrder(int rotationOrder) {
        this.rotationOrder = rotationOrder;
    }
}
