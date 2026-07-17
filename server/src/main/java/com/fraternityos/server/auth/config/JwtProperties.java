package com.fraternityos.server.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code app.jwt.*} from configuration. {@code secret} must be at least
 * 256 bits (32 bytes) for HS256.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expirationMs) {
}
