package com.fraternityos.server.finance.application;

/** Thrown when a statement already exists for the given house, year, and month. */
public class DuplicateStatementException extends RuntimeException {

    public DuplicateStatementException(int month, int year) {
        super("A statement already exists for " + month + "/" + year + ".");
    }
}
