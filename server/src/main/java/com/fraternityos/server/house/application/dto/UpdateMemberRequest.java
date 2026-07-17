package com.fraternityos.server.house.application.dto;

import com.fraternityos.server.house.domain.MemberStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Updates a member's profile, status, and assigned positions. {@code positions}
 * fully replaces the member's current set; unknown names are ignored.
 */
public record UpdateMemberRequest(
        @NotBlank String name,
        @NotNull MemberStatus status,
        String phone,
        String room,
        List<String> positions) {
}
