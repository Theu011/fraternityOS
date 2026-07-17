package com.fraternityos.server.calendar.application;

/** Thrown when an event is absent or belongs to a different house. */
public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(Long id) {
        super("Event not found: " + id);
    }
}
