FROM eclipse-temurin:17-jdk-alpine

VOLUME /tmp

# Copy the JAR file from the target directory to the Docker image
COPY target/userAuthentication-0.0.1-SNAPSHOT.jar app.jar

# Set the entry point for the Docker container
ENTRYPOINT ["java","-jar","/app.jar"]

# Expose the port that your application is listening on
EXPOSE 10001
