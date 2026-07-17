package com.fraternityos.server.support;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base for full-stack integration tests. Boots the whole application against a
 * throwaway PostgreSQL container (so the real Flyway migrations run and the
 * PG-specific schema is exercised) and auto-configures {@code MockMvc}.
 *
 * <p>The container follows the <em>singleton</em> pattern: it is started once in
 * a static initializer and shared by every integration-test class, staying up
 * for the whole JVM run (Testcontainers' Ryuk reaps it at exit). This avoids the
 * {@code @Container}/{@code @Testcontainers} per-class lifecycle, which would
 * stop a shared static container after the first class and leave later classes
 * with a dead datasource. {@code @ServiceConnection} wires the container into
 * the datasource, replacing the {@code localhost} config from
 * {@code application.yml}.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine");

    static {
        POSTGRES.start();
    }
}
