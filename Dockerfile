# =============================================================================
# Stage 1 — Build
# =============================================================================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true -q

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
