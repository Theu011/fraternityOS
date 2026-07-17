package com.fraternityos.server.auth.security;

import java.util.Set;

/**
 * Principal stored in the {@code SecurityContext} for an authenticated request.
 * Identity is the {@code userId}; when the user belongs to a house the
 * house-scoped fields ({@code membershipId}, {@code houseId}) and the held
 * {@code positions} are populated from the verified token — never from client
 * input. A user without a house has null house-scoped fields and no positions.
 */
public record AuthenticatedMember(
        Long userId,
        Long membershipId,
        Long houseId,
        String email,
        Set<String> positions) {

    /** Whether the caller holds the permission-granting President position. */
    public boolean isPresident() {
        return positions != null && positions.contains("President");
    }
}
