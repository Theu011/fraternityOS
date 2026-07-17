package com.fraternityos.server.house.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Onboarding: create a new house. The creator becomes its founding President. */
public record CreateHouseRequest(
        @NotBlank @Size(max = 150) String name) {
}
