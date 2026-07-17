package com.fraternityos.server.announcement.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAnnouncementRequest(
        @NotBlank String title,
        @NotBlank String content,
        boolean pinned) {
}
