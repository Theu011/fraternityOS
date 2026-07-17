package com.fraternityos.server.responsibility.application.dto;

import com.fraternityos.server.responsibility.domain.AssignmentStatus;
import java.time.Instant;
import java.time.LocalDate;

/**
 * A chore for the board: a fixed responsibility joined with its assignment.
 * {@code status} is the effective status — PENDING assignments past their due
 * date are reported as OVERDUE.
 */
public record ChoreResponse(
        Long id,
        String title,
        String description,
        Long assigneeId,
        String assigneeName,
        LocalDate dueDate,
        AssignmentStatus status,
        Instant completedAt) {
}
