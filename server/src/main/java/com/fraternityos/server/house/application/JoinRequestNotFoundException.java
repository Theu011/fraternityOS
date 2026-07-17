package com.fraternityos.server.house.application;

/** Raised when a join request does not exist or does not belong to the caller's house. */
public class JoinRequestNotFoundException extends RuntimeException {

    public JoinRequestNotFoundException(Long id) {
        super("Join request not found: " + id);
    }
}
