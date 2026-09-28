# Multi-stage Dockerfile for RepoVision Spring Boot Backend
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /workspace

# Cache Maven wrapper and dependencies
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source and build executable JAR
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# Production Runtime Image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Install Git CLI (required for RepoVision GitCliClient ProcessBuilder execution)
RUN apt-get update && \
    apt-get install -y --no-install-recommends git ca-certificates curl && \
    rm -rf /var/lib/apt/lists/*

# Add non-root application user for container security
RUN groupadd -r repovision && useradd -r -g repovision -m repovision
USER repovision

# Copy built JAR from builder
COPY --from=builder /workspace/target/*.jar app.jar

# Expose HTTP port
EXPOSE 8080

# Environment variables with sensible defaults
ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080 \
    JAVA_OPTS="-Xms256m -Xmx1024m -XX:+UseG1GC"

# Healthcheck via Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
