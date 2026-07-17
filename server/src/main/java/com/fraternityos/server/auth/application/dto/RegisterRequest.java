package com.fraternityos.server.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Self-signup. Creates a user account only — the new user belongs to no house
 * and reaches onboarding (create or join a house) after logging in.
 */
public record RegisterRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @Size(min = 8, message = "password must be at least 8 characters") String password) {
}
