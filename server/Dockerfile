# syntax=docker/dockerfile:1

# --- Build stage: compile and package the jar (tests run in CI, skipped here) ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies first: only re-resolved when pom.xml changes.
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# --- Runtime stage: slim Alpine JRE, non-root, just the jar ---
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Create an unprivileged user and give it ownership of /app so it can write the
# uploads dir (and any named volume mounted there inherits this ownership).
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /app/uploads && chown -R spring:spring /app
USER spring

# Optional JVM tuning, e.g. JAVA_OPTS="-XX:MaxRAMPercentage=75".
ENV JAVA_OPTS=""

EXPOSE 8080

# Liveness probe via the actuator health endpoint (permitAll in SecurityConfig).
HEALTHCHECK --interval=30s --timeout=3s --start-period=45s --retries=3 \
    CMD wget -q -O - http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
