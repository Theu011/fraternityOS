package com.fraternityos.server.house.api;

import com.fraternityos.server.auth.security.AuthenticatedMember;
import com.fraternityos.server.house.application.MemberService;
import com.fraternityos.server.house.application.dto.AssignPositionsRequest;
import com.fraternityos.server.house.application.dto.ChangeStatusRequest;
import com.fraternityos.server.house.application.dto.CreateMemberRequest;
import com.fraternityos.server.house.application.dto.MemberResponse;
import com.fraternityos.server.house.application.dto.UpdateMemberRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Member CRUD. Reads are open to any authenticated member of the house; writes
 * are President-only. The house is always taken from the token principal.
 */
@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberResponse> list(@AuthenticationPrincipal AuthenticatedMember principal) {
        return memberService.list(principal.houseId());
    }

    /** Alumni of the house. */
    @GetMapping("/alumni")
    public List<MemberResponse> alumni(@AuthenticationPrincipal AuthenticatedMember principal) {
        return memberService.listAlumni(principal.houseId());
    }

    @GetMapping("/{id}")
    public MemberResponse get(@AuthenticationPrincipal AuthenticatedMember principal,
                              @PathVariable Long id) {
        return memberService.get(principal.houseId(), id);
    }

    /** Change a member's status (ACTIVE ↔ ALUMNI). President only. */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('PRESIDENT')")
    public MemberResponse changeStatus(@AuthenticationPrincipal AuthenticatedMember principal,
                                       @PathVariable Long id,
                                       @Valid @RequestBody ChangeStatusRequest request) {
        return memberService.changeStatus(principal.houseId(), id, request.status());
    }

    /** Assign one or more positions to a member. President only. */
    @PostMapping("/{id}/positions")
    @PreAuthorize("hasRole('PRESIDENT')")
    public MemberResponse assignPositions(@AuthenticationPrincipal AuthenticatedMember principal,
                                          @PathVariable Long id,
                                          @Valid @RequestBody AssignPositionsRequest request) {
        return memberService.assignPositions(principal.houseId(), id, request.positionIds());
    }

    /** Remove a position from a member. President only. */
    @DeleteMapping("/{id}/positions/{positionId}")
    @PreAuthorize("hasRole('PRESIDENT')")
    public MemberResponse removePosition(@AuthenticationPrincipal AuthenticatedMember principal,
                                         @PathVariable Long id,
                                         @PathVariable Long positionId) {
        return memberService.removePosition(principal.houseId(), id, positionId);
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse create(@AuthenticationPrincipal AuthenticatedMember principal,
                                 @Valid @RequestBody CreateMemberRequest request) {
        return memberService.create(principal.houseId(), request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    public MemberResponse update(@AuthenticationPrincipal AuthenticatedMember principal,
                                 @PathVariable Long id,
                                 @Valid @RequestBody UpdateMemberRequest request) {
        return memberService.update(principal.houseId(), id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthenticatedMember principal,
                       @PathVariable Long id) {
        memberService.delete(principal.houseId(), id);
    }
}
