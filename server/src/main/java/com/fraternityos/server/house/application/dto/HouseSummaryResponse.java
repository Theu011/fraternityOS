package com.fraternityos.server.house.application.dto;

import com.fraternityos.server.house.infrastructure.HouseSummary;
import java.time.Instant;

/** House listing/search entry: basic info plus how many active members it has. */
public record HouseSummaryResponse(Long id, String name, long activeMemberCount, Instant createdAt) {

    public static HouseSummaryResponse from(HouseSummary summary) {
        return new HouseSummaryResponse(
                summary.getId(), summary.getName(),
                summary.getActiveMemberCount(), summary.getCreatedAt());
    }
}
