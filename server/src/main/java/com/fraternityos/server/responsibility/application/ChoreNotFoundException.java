package com.fraternityos.server.responsibility.application;

/** Thrown when a chore is absent or belongs to a different house. */
public class ChoreNotFoundException extends RuntimeException {

    public ChoreNotFoundException(Long id) {
        super("Chore not found: " + id);
    }
}
