package com.fraternityos.server.responsibility.domain;

/**
 * Status of a single responsibility assignment. The scheduled worker flips
 * PENDING to OVERDUE once the due date passes.
 */
public enum AssignmentStatus {
    PENDING,
    COMPLETED,
    OVERDUE
}
