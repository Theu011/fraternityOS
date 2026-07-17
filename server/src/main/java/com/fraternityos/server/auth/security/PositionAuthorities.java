package com.fraternityos.server.auth.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Maps the positions held by a membership to Spring Security authorities.
 * Authorization derives from positions: President/Treasurer grant elevated
 * authorities, and anyone with a membership gets the {@code RESIDENT} baseline.
 */
public final class PositionAuthorities {

    /** Position name → the extra role authority it grants. */
    private static final Map<String, String> GRANTING_POSITIONS = Map.of(
            "President", "PRESIDENT",
            "Treasurer", "TREASURER");

    private PositionAuthorities() {
    }

    /**
     * Builds the authorities for a caller. A member always has {@code ROLE_RESIDENT};
     * President/Treasurer positions add {@code ROLE_PRESIDENT}/{@code ROLE_TREASURER}.
     * A user with no membership (no positions) gets no authorities.
     */
    public static List<GrantedAuthority> forPositions(Collection<String> positions,
                                                      boolean hasMembership) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (hasMembership) {
            authorities.add(new SimpleGrantedAuthority("ROLE_RESIDENT"));
        }
        if (positions != null) {
            for (String position : positions) {
                String role = GRANTING_POSITIONS.get(position);
                if (role != null) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }
        }
        return authorities;
    }
}
