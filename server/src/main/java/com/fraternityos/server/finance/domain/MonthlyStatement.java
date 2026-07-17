package com.fraternityos.server.finance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * A monthly rent statement for a house. {@code attachmentUrl} holds a RELATIVE
 * storage key (never an absolute path), resolved by the FileStorageService.
 */
@Entity
@Table(name = "monthly_statement")
public class MonthlyStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "house_id", nullable = false)
    private Long houseId;

    @Column(name = "uploaded_by_membership_id", nullable = false)
    private Long uploadedByMembershipId;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private int year;

    private String notes;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected MonthlyStatement() {
        // for JPA
    }

    public MonthlyStatement(Long houseId, Long uploadedByMembershipId, int month, int year) {
        this.houseId = houseId;
        this.uploadedByMembershipId = uploadedByMembershipId;
        this.month = month;
        this.year = year;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getHouseId() {
        return houseId;
    }

    public Long getUploadedByMembershipId() {
        return uploadedByMembershipId;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
