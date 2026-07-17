package com.fraternityos.server.calendar.application;

import com.fraternityos.server.calendar.application.dto.CreateEventRequest;
import com.fraternityos.server.calendar.application.dto.EventResponse;
import com.fraternityos.server.calendar.application.dto.UpdateEventRequest;
import com.fraternityos.server.calendar.domain.Event;
import com.fraternityos.server.calendar.infrastructure.EventRepository;
import com.fraternityos.server.house.infrastructure.MemberName;
import com.fraternityos.server.house.infrastructure.MembershipRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Calendar event CRUD, scoped to the caller's house. The API is date-only
 * ({@link LocalDate}); dates are stored as UTC-midnight instants so the day is
 * stable regardless of the viewer's timezone.
 */
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final MembershipRepository membershipRepository;

    public EventService(EventRepository eventRepository, MembershipRepository membershipRepository) {
        this.eventRepository = eventRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional(readOnly = true)
    public List<EventResponse> list(Long houseId) {
        Map<Long, String> names = membershipRepository.findMemberNamesByHouseId(houseId).stream()
                .collect(Collectors.toMap(MemberName::getMembershipId, MemberName::getName));
        return eventRepository.findAllByHouseIdOrderByStartDateAsc(houseId).stream()
                .map(e -> EventResponse.from(e, names.get(e.getCreatedByMembershipId())))
                .toList();
    }

    @Transactional
    public EventResponse create(Long houseId, Long createdByMembershipId, CreateEventRequest request) {
        Event event = new Event(houseId, createdByMembershipId, request.title(), toInstant(request.date()));
        event.setDescription(request.description());
        return EventResponse.from(eventRepository.save(event),
                creatorName(houseId, createdByMembershipId));
    }

    @Transactional
    public EventResponse update(Long houseId, Long id, UpdateEventRequest request) {
        Event event = require(houseId, id);
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setStartDate(toInstant(request.date()));
        return EventResponse.from(eventRepository.save(event),
                creatorName(houseId, event.getCreatedByMembershipId()));
    }

    @Transactional
    public void delete(Long houseId, Long id) {
        eventRepository.delete(require(houseId, id));
    }

    private Event require(Long houseId, Long id) {
        return eventRepository.findByIdAndHouseId(id, houseId)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    private Instant toInstant(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    private String creatorName(Long houseId, Long membershipId) {
        return membershipRepository.findMemberName(membershipId, houseId).orElse(null);
    }
}
