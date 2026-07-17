package com.fraternityos.server.finance.domain;

/**
 * Payment status for a member's share of a monthly statement. The self-report
 * vs. treasurer-set flow (and a possible OVERDUE status) is an open question.
 */
public enum PaymentStatus {
    PENDING,
    PAID
}
