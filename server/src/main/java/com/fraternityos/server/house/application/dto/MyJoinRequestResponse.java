package com.fraternityos.server.house.application.dto;

import com.fraternityos.server.house.domain.JoinRequestStatus;
import com.fraternityos.server.house.infrastructure.JoinRequestRepository.MyJoinRequest;
import java.time.Instant;

/** A join request the caller has submitted, with the target house name. */
public record MyJoinRequestResponse(
        Long id, Long houseId, String houseName, JoinRequestStatus status, Instant createdAt) {

    public static MyJoinRequestResponse from(MyJoinRequest r) {
        return new MyJoinRequestResponse(
                r.getId(), r.getHouseId(), r.getHouseName(), r.getStatus(), r.getCreatedAt());
    }
}
