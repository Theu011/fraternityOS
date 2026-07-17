package com.fraternityos.server.finance.application;

/** Thrown when the caller has no payment row for a statement. */
public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(Long statementId) {
        super("No payment found for the current member on statement " + statementId + ".");
    }
}
