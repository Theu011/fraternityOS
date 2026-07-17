package com.fraternityos.server.house.application;

/** Thrown when a member is absent or belongs to a different house. */
public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(Long id) {
        super("Member not found: " + id);
    }
}
