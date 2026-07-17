package com.fraternityos.server.calendar.application.dto;

import com.fraternityos.server.calendar.domain.Event;
import java.time.LocalDate;
import java.time.ZoneOffset;

public record EventResponse(
        Long id,
        String title,
        String description,
        LocalDate date,
        Long createdByMembershipId,
        String createdByName) {

    public static EventResponse from(Event event, String createdByName) {
        LocalDate date = event.getStartDate().atZone(ZoneOffset.UTC).toLocalDate();
        return new EventResponse(event.getId(), event.getTitle(), event.getDescription(),
                date, event.getCreatedByMembershipId(), createdByName);
    }
}
