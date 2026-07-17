package com.fraternityos.server.house.api;

import com.fraternityos.server.auth.application.dto.AuthResponse;
import com.fraternityos.server.auth.security.AuthenticatedMember;
import com.fraternityos.server.house.application.HouseService;
import com.fraternityos.server.house.application.dto.CreateHouseRequest;
import com.fraternityos.server.house.application.dto.HouseResponse;
import com.fraternityos.server.house.application.dto.HouseSummaryResponse;
import com.fraternityos.server.house.application.dto.JoinRequestResponse;
import com.fraternityos.server.house.application.dto.MyJoinRequestResponse;
import com.fraternityos.server.house.application.dto.PendingJoinRequestResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/houses")
public class HouseController {

    private final HouseService houseService;

    public HouseController(HouseService houseService) {
        this.houseService = houseService;
    }

    /** All houses (onboarding directory), optionally filtered by name. */
    @GetMapping
    public List<HouseSummaryResponse> list(@RequestParam(required = false) String name) {
        return houseService.list(name);
    }

    /** Search houses by name fragment (alias of {@code GET /houses?name=}). */
    @GetMapping("/search")
    public List<HouseSummaryResponse> search(@RequestParam(required = false) String name) {
        return houseService.list(name);
    }

    /** The authenticated member's own house. */
    @GetMapping("/current")
    public HouseResponse current(@AuthenticationPrincipal AuthenticatedMember principal) {
        return houseService.getCurrent(principal.houseId());
    }

    /** Create a house; the caller becomes its President. Returns a fresh token. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse create(@AuthenticationPrincipal AuthenticatedMember principal,
                               @Valid @RequestBody CreateHouseRequest request) {
        return houseService.create(principal.userId(), request.name());
    }

    /** Submit a request for the caller to join a house. */
    @PostMapping("/{id}/join-request")
    @ResponseStatus(HttpStatus.CREATED)
    public JoinRequestResponse requestToJoin(@AuthenticationPrincipal AuthenticatedMember principal,
                                             @PathVariable Long id) {
        return houseService.requestToJoin(principal.userId(), id);
    }

    /** The caller's own submitted join requests and their status. */
    @GetMapping("/join-requests/mine")
    public List<MyJoinRequestResponse> myJoinRequests(
            @AuthenticationPrincipal AuthenticatedMember principal) {
        return houseService.myJoinRequests(principal.userId());
    }

    /** Pending requests to the President's house, awaiting a decision. */
    @GetMapping("/join-requests")
    @PreAuthorize("hasRole('PRESIDENT')")
    public List<PendingJoinRequestResponse> pendingJoinRequests(
            @AuthenticationPrincipal AuthenticatedMember principal) {
        return houseService.pendingJoinRequests(principal.houseId());
    }

    /** Approve a pending request; the requester becomes an ACTIVE member. */
    @PostMapping("/join-requests/{id}/approve")
    @PreAuthorize("hasRole('PRESIDENT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approve(@AuthenticationPrincipal AuthenticatedMember principal,
                        @PathVariable Long id) {
        houseService.approve(principal.houseId(), id);
    }

    /** Reject a pending request. */
    @PostMapping("/join-requests/{id}/reject")
    @PreAuthorize("hasRole('PRESIDENT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(@AuthenticationPrincipal AuthenticatedMember principal,
                       @PathVariable Long id) {
        houseService.reject(principal.houseId(), id);
    }
}
