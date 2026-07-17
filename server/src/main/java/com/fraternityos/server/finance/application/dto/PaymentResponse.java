package com.fraternityos.server.finance.application.dto;

import com.fraternityos.server.finance.domain.Payment;
import com.fraternityos.server.finance.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

/** A single member's payment for a statement (treasurer/president overview). */
public record PaymentResponse(
        Long membershipId,
        String memberName,
        BigDecimal amount,
        PaymentStatus status,
        Instant paidAt) {

    public static PaymentResponse from(Payment payment, String memberName) {
        return new PaymentResponse(payment.getMembershipId(), memberName, payment.getAmount(),
                payment.getStatus(), payment.getPaidAt());
    }
}
