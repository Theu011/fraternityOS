package com.fraternityos.server.calendar.api;

import com.fraternityos.server.auth.security.AuthenticatedMember;
import com.fraternityos.server.calendar.application.EventService;
import com.fraternityos.server.calendar.application.dto.CreateEventRequest;
import com.fraternityos.server.calendar.application.dto.EventResponse;
import com.fraternityos.server.calendar.application.dto.UpdateEventRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Shared house calendar. All members can read; only Presidents can create,
 * edit, or delete events.
 */
@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventResponse> list(@AuthenticationPrincipal AuthenticatedMember principal) {
        return eventService.list(principal.houseId());
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(@AuthenticationPrincipal AuthenticatedMember principal,
                                @Valid @RequestBody CreateEventRequest request) {
        return eventService.create(principal.houseId(), principal.membershipId(), request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    public EventResponse update(@AuthenticationPrincipal AuthenticatedMember principal,
                                @PathVariable Long id,
                                @Valid @RequestBody UpdateEventRequest request) {
        return eventService.update(principal.houseId(), id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthenticatedMember principal,
                       @PathVariable Long id) {
        eventService.delete(principal.houseId(), id);
    }
}
