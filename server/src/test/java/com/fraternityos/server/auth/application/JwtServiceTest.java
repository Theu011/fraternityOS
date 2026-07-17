package com.fraternityos.server.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fraternityos.server.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.util.List;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "test-secret-that-is-at-least-32-bytes-long-000";

    private JwtService jwtService(long expirationMs) {
        return new JwtService(new JwtProperties(SECRET, expirationMs));
    }

    private String token(JwtService service) {
        return service.generateToken(7L, 3L, 42L, "mat@x.com", List.of("President"));
    }

    @Test
    void generatesAndParsesTokenRoundTrip() {
        JwtService service = jwtService(60_000);

        Claims claims = service.parse(token(service));

        assertThat(claims.getSubject()).isEqualTo("7");
        assertThat(claims.get("membershipId", Long.class)).isEqualTo(3L);
        assertThat(claims.get("houseId", Long.class)).isEqualTo(42L);
        assertThat(claims.get("email", String.class)).isEqualTo("mat@x.com");
        assertThat(service.positionsOf(claims)).containsExactly("President");
    }

    @Test
    void rejectsTamperedToken() {
        JwtService service = jwtService(60_000);
        String token = token(service);
        String tampered = token.substring(0, token.length() - 2)
                + (token.endsWith("a") ? "bb" : "aa");

        assertThatThrownBy(() -> service.parse(tampered))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void rejectsExpiredToken() {
        JwtService service = jwtService(-1_000); // already expired
        String token = token(service);

        assertThatThrownBy(() -> service.parse(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
