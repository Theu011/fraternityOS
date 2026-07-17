package com.fraternityos.server.announcement.application;

/** Thrown when an announcement is absent or belongs to a different house. */
public class AnnouncementNotFoundException extends RuntimeException {

    public AnnouncementNotFoundException(Long id) {
        super("Announcement not found: " + id);
    }
}
