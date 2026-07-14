# =============================================================================
# Stage 1 — Build
# =============================================================================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only pom.xml first — Docker caches this layer separately so
# dependency downloads are NOT re-run on every source code change.
COPY pom.xml .

# Download all dependencies with retry logic.
# -B            = batch/non-interactive mode (better for CI)
# retryHandler  = retry each artifact up to 5 times on network errors
# reconnect     = re-open the connection on failure before retrying
# The || loop retries the ENTIRE goal up to 3 times in case of a
# mid-stream disconnect (like the 42 MB openai-java-core artifact drop).
RUN for i in 1 2 3; do \
      mvn dependency:go-offline -B \
        -Dmaven.wagon.http.retryHandler.count=5 \
        -Dmaven.wagon.http.retryHandler.requestSentEnabled=true \
        -Dmaven.wagon.http.reconnectAttemptCount=3 \
      && break; \
      echo "Attempt $i failed, retrying in 15s..."; \
      sleep 15; \
    done

COPY src ./src

# Build the fat JAR (tests skipped — run them in CI separately).
RUN mvn clean package -B -Dmaven.test.skip=true \
      -Dmaven.wagon.http.retryHandler.count=5 \
      -Dmaven.wagon.http.retryHandler.requestSentEnabled=true

# =============================================================================
# Stage 2 — Runtime
# Files are now stored in S3 — no local upload directory needed.
# =============================================================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# curl is required by the ECS container health check command.
# Alpine doesn't include it by default in the temurin image.
RUN apk add --no-cache curl

COPY --from=build /app/target/cfc-platform-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 9090

ENTRYPOINT ["java", \
  "-Xms256m", "-Xmx512m", \
  "-jar", "app.jar"]
