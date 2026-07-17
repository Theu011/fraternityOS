package com.fraternityos.server.finance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/** A single member's share of a monthly statement. */
@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "monthly_statement_id", nullable = false)
    private Long monthlyStatementId;

    @Column(name = "membership_id", nullable = false)
    private Long membershipId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Payment() {
        // for JPA
    }

    public Payment(Long monthlyStatementId, Long membershipId, BigDecimal amount) {
        this.monthlyStatementId = monthlyStatementId;
        this.membershipId = membershipId;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    /** Mark this payment paid, recording the payment instant. */
    public void markPaid() {
        this.status = PaymentStatus.PAID;
        this.paidAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getMonthlyStatementId() {
        return monthlyStatementId;
    }

    public Long getMembershipId() {
        return membershipId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
