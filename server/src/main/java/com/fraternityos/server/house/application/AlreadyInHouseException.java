package com.fraternityos.server.house.application;

/** Raised when a user who already belongs to a house tries to create or join one. */
public class AlreadyInHouseException extends RuntimeException {

    public AlreadyInHouseException() {
        super("User already belongs to a house");
    }
}
