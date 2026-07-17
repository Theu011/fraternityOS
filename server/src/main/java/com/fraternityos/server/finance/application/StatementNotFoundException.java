package com.fraternityos.server.finance.application;

/** Thrown when a statement is absent or belongs to a different house. */
public class StatementNotFoundException extends RuntimeException {

    public StatementNotFoundException(Long id) {
        super("Statement not found: " + id);
    }
}
