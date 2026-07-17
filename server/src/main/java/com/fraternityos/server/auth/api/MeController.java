package com.fraternityos.server.auth.api;

import com.fraternityos.server.auth.security.AuthenticatedMember;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Secured endpoints proving token authentication and role gating. */
@RestController
@RequestMapping("/me")
public class MeController {

    /** Any authenticated member: echoes the identity carried by the token. */
    @GetMapping
    public AuthenticatedMember me(@AuthenticationPrincipal AuthenticatedMember principal) {
        return principal;
    }

    /** Role-gated demo: only members whose token carries ROLE_PRESIDENT may call this. */
    @GetMapping("/president")
    @PreAuthorize("hasRole('PRESIDENT')")
    public AuthenticatedMember presidentOnly(@AuthenticationPrincipal AuthenticatedMember principal) {
        return principal;
    }
}
