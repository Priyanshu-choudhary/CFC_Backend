# Use the official Maven image to create a build artifact.
FROM maven:3.8.1-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# Use the official OpenJDK image to run the application
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/Basic_CRUD_Oprations-0.0.1-SNAPSHOT.jar ./your-app.jar
ENTRYPOINT ["java", "-jar", "your-app.jar"]
