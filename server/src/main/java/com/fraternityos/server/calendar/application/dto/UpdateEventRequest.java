package com.fraternityos.server.calendar.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdateEventRequest(
        @NotBlank String title,
        @NotNull LocalDate date,
        String description) {
}
