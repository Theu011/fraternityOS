package com.fraternityos.server.house.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * President-driven creation of a member: creates the user account and an ACTIVE
 * membership in the President's house. {@code positions} are catalog names
 * (e.g. "Treasurer"); unknown names are ignored.
 */
public record CreateMemberRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @Size(min = 8, message = "password must be at least 8 characters") String password,
        String phone,
        String room,
        List<String> positions) {
}
