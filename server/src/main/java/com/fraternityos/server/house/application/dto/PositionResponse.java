package com.fraternityos.server.house.application.dto;

import com.fraternityos.server.house.domain.Position;

/** A catalog position available to assign to members. */
public record PositionResponse(Long id, String name, String description) {

    public static PositionResponse from(Position position) {
        return new PositionResponse(position.getId(), position.getName(), position.getDescription());
    }
}
