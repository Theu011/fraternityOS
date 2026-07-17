package com.fraternityos.server.house.application.dto;

import com.fraternityos.server.house.domain.JoinRequest;
import com.fraternityos.server.house.domain.JoinRequestStatus;
import java.time.Instant;

public record JoinRequestResponse(Long id, Long houseId, JoinRequestStatus status, Instant createdAt) {

    public static JoinRequestResponse from(JoinRequest request) {
        return new JoinRequestResponse(
                request.getId(), request.getHouseId(), request.getStatus(), request.getCreatedAt());
    }
}
