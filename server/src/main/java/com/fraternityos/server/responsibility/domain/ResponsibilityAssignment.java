package com.fraternityos.server.responsibility.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

/** A single occurrence of a responsibility assigned to a member for a period. */
@Entity
@Table(name = "responsibility_assignment")
public class ResponsibilityAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "responsibility_id", nullable = false)
    private Long responsibilityId;

    @Column(name = "membership_id", nullable = false)
    private Long membershipId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus status;

    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected ResponsibilityAssignment() {
        // for JPA
    }

    public ResponsibilityAssignment(Long responsibilityId, Long membershipId,
                                    LocalDate assignedDate, LocalDate dueDate) {
        this.responsibilityId = responsibilityId;
        this.membershipId = membershipId;
        this.assignedDate = assignedDate;
        this.dueDate = dueDate;
        this.status = AssignmentStatus.PENDING;
    }

    /** Mark this assignment completed, recording the completion instant. */
    public void markCompleted() {
        this.status = AssignmentStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getResponsibilityId() {
        return responsibilityId;
    }

    public Long getMembershipId() {
        return membershipId;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
