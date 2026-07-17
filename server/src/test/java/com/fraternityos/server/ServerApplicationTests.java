package com.fraternityos.server;

import com.fraternityos.server.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Smoke test: the full application context boots (against a Testcontainers
 * PostgreSQL, so Flyway migrations apply and Hibernate {@code validate} passes).
 */
class ServerApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
    }
}
