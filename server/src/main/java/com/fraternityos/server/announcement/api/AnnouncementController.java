package com.fraternityos.server.announcement.api;

import com.fraternityos.server.announcement.application.AnnouncementService;
import com.fraternityos.server.announcement.application.dto.AnnouncementResponse;
import com.fraternityos.server.announcement.application.dto.CreateAnnouncementRequest;
import com.fraternityos.server.announcement.application.dto.UpdateAnnouncementRequest;
import com.fraternityos.server.auth.security.AuthenticatedMember;
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
 * Announcements. All house members can read; only Presidents can create, edit,
 * pin, or delete — the RBAC split for Task 5.
 */
@RestController
@RequestMapping("/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public List<AnnouncementResponse> list(@AuthenticationPrincipal AuthenticatedMember principal) {
        return announcementService.list(principal.houseId());
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public AnnouncementResponse create(@AuthenticationPrincipal AuthenticatedMember principal,
                                       @Valid @RequestBody CreateAnnouncementRequest request) {
        return announcementService.create(principal.houseId(), principal.membershipId(), request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    public AnnouncementResponse update(@AuthenticationPrincipal AuthenticatedMember principal,
                                       @PathVariable Long id,
                                       @Valid @RequestBody UpdateAnnouncementRequest request) {
        return announcementService.update(principal.houseId(), id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthenticatedMember principal,
                       @PathVariable Long id) {
        announcementService.delete(principal.houseId(), id);
    }
}
