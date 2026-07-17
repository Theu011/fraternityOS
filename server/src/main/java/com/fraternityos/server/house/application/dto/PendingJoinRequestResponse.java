package com.fraternityos.server.house.application.dto;

import com.fraternityos.server.house.infrastructure.JoinRequestRepository.PendingJoinRequest;
import java.time.Instant;

/** A pending request to join the President's house, with requester identity. */
public record PendingJoinRequestResponse(
        Long id, Long userId, String name, String email, Instant createdAt) {

    public static PendingJoinRequestResponse from(PendingJoinRequest r) {
        return new PendingJoinRequestResponse(
                r.getId(), r.getUserId(), r.getName(), r.getEmail(), r.getCreatedAt());
    }
}
