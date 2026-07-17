package com.fraternityos.server.finance.application.dto;

import com.fraternityos.server.finance.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A monthly statement plus the caller's own payment status and the paid/total
 * roll-up across members.
 */
public record StatementResponse(
        Long id,
        int month,
        int year,
        String notes,
        String uploadedByName,
        Instant createdAt,
        boolean hasAttachment,
        PaymentStatus myStatus,
        BigDecimal myAmount,
        long paidCount,
        long totalCount) {
}
