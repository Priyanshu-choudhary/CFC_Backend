FROM maven:3.8.1-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/cfc-platform-0.0.1-SNAPSHOT.jar ./app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
