package com.fraternityos.server.house.application.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Assign one or more catalog positions (by id) to a member. */
public record AssignPositionsRequest(@NotEmpty List<Long> positionIds) {
}
