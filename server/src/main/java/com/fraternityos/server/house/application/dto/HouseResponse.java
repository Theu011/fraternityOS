package com.fraternityos.server.house.application.dto;

import com.fraternityos.server.house.domain.House;
import java.time.Instant;

public record HouseResponse(Long id, String name, Instant createdAt) {

    public static HouseResponse from(House house) {
        return new HouseResponse(house.getId(), house.getName(), house.getCreatedAt());
    }
}
