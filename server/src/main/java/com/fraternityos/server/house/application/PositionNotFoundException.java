package com.fraternityos.server.house.application;

/** Raised when a referenced catalog position does not exist. */
public class PositionNotFoundException extends RuntimeException {

    public PositionNotFoundException(Long id) {
        super("Position not found: " + id);
    }
}
