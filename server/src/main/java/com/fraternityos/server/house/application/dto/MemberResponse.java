package com.fraternityos.server.house.application.dto;

import com.fraternityos.server.house.domain.MemberStatus;
import com.fraternityos.server.house.domain.Membership;
import com.fraternityos.server.house.domain.User;
import java.time.Instant;
import java.util.List;

/**
 * Public view of a house member: the membership joined with its user's profile
 * and held positions. {@code id} is the membership id; it never exposes the
 * password hash.
 */
public record MemberResponse(
        Long id,
        Long userId,
        String name,
        String email,
        String phone,
        String room,
        MemberStatus status,
        List<String> positions,
        Instant joinedAt) {

    public static MemberResponse from(Membership membership, User user, List<String> positions) {
        return new MemberResponse(
                membership.getId(),
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                membership.getRoom(),
                membership.getStatus(),
                positions,
                membership.getJoinedAt());
    }
}
