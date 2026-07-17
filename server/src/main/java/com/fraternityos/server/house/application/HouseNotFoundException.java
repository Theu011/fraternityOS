package com.fraternityos.server.house.application;

/** Thrown when the authenticated principal's house cannot be found. */
public class HouseNotFoundException extends RuntimeException {

    public HouseNotFoundException(Long id) {
        super("House not found: " + id);
    }
}
