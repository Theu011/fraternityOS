package com.fraternityos.server.responsibility.api;

import com.fraternityos.server.auth.security.AuthenticatedMember;
import com.fraternityos.server.responsibility.application.ChoreService;
import com.fraternityos.server.responsibility.application.dto.ChoreResponse;
import com.fraternityos.server.responsibility.application.dto.CreateChoreRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chores")
public class ChoreController {

    private final ChoreService choreService;

    public ChoreController(ChoreService choreService) {
        this.choreService = choreService;
    }

    @GetMapping
    public List<ChoreResponse> list(@AuthenticationPrincipal AuthenticatedMember principal) {
        return choreService.list(principal.houseId());
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public ChoreResponse create(@AuthenticationPrincipal AuthenticatedMember principal,
                                @Valid @RequestBody CreateChoreRequest request) {
        return choreService.create(principal.houseId(), request);
    }

    /** Marks a chore done. Allowed for the assignee or a President (enforced in the service). */
    @PostMapping("/{id}/complete")
    public ChoreResponse complete(@AuthenticationPrincipal AuthenticatedMember principal,
                                  @PathVariable Long id) {
        return choreService.complete(principal.houseId(), id, principal.membershipId(),
                principal.isPresident());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthenticatedMember principal,
                       @PathVariable Long id) {
        choreService.delete(principal.houseId(), id);
    }
}
