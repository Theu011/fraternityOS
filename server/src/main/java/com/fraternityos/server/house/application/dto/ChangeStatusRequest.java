package com.fraternityos.server.house.application.dto;

import com.fraternityos.server.house.domain.MemberStatus;
import jakarta.validation.constraints.NotNull;

/** Change a member's lifecycle status (ACTIVE ↔ ALUMNI). */
public record ChangeStatusRequest(@NotNull MemberStatus status) {
}
