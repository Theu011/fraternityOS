package com.fraternityos.server.auth.application.dto;

import java.util.List;

/**
 * Result of a successful register/login. Carries the bearer token plus the
 * caller's identity and, when they belong to a house, the house-scoped context
 * (membership, house, held positions).
 */
public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        Long membershipId,
        Long houseId,
        String name,
        List<String> positions) {

    public static AuthResponse bearer(String token, Long userId, Long membershipId,
                                      Long houseId, String name, List<String> positions) {
        return new AuthResponse(token, "Bearer", userId, membershipId, houseId, name, positions);
    }
}
