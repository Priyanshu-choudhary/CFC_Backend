# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-slim

# Set the working directory in the container
WORKDIR /app

# Copy the jar file to the container
COPY target/Basic_CRUD_Oprations-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 9090

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
