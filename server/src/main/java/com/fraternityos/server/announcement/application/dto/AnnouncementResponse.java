package com.fraternityos.server.announcement.application.dto;

import com.fraternityos.server.announcement.domain.Announcement;
import java.time.Instant;

public record AnnouncementResponse(
        Long id,
        String title,
        String content,
        boolean pinned,
        Long authorMembershipId,
        String authorName,
        Instant createdAt,
        Instant updatedAt) {

    public static AnnouncementResponse from(Announcement a, String authorName) {
        return new AnnouncementResponse(a.getId(), a.getTitle(), a.getContent(), a.isPinned(),
                a.getAuthorMembershipId(), authorName, a.getCreatedAt(), a.getUpdatedAt());
    }
}
