package com.fraternityos.server.auth.application;

import com.fraternityos.server.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Issues and verifies stateless HS256 JWTs. The subject is the user id;
 * {@code membershipId}, {@code houseId}, {@code email} and the held
 * {@code positions} travel as custom claims so a request can be authorized
 * (positions drive authority) without a database lookup.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.expirationMs = properties.expirationMs();
        if (properties.secret().contains("change-me")) {
            log.warn("Using the built-in development JWT secret. Set the JWT_SECRET "
                    + "environment variable to a strong, unique value before deploying.");
        }
    }

    public String generateToken(Long userId, Long membershipId, Long houseId, String email,
                                Collection<String> positions) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("membershipId", membershipId)
                .claim("houseId", houseId)
                .claim("email", email)
                .claim("positions", List.copyOf(positions))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key)
                .compact();
    }

    /** Verifies signature and expiry, returning the parsed claims. */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @SuppressWarnings("unchecked")
    public List<String> positionsOf(Claims claims) {
        Object raw = claims.get("positions");
        if (raw instanceof Collection<?> collection) {
            return collection.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
