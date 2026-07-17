package com.fraternityos.server.auth.application;

/** Thrown when registration is attempted with an email that already exists. */
public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException(String email) {
        super("Email already in use: " + email);
    }
}
