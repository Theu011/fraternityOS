package com.fraternityos.server.house.application;

/**
 * Raised when an operation would leave the house with no active President
 * (removing the position from, or retiring, the last one).
 */
public class LastPresidentException extends RuntimeException {

    public LastPresidentException() {
        super("The house must always have at least one active President");
    }
}
