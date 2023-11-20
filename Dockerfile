FROM eclipse-temurin:17-jdk-alpine

# Create a directory inside the container to store the JAR file
WORKDIR /app

# Copy the JAR file from your local machine to the container
COPY /target/userAuthentication-0.0.1-SNAPSHOT.jar app.jar

# Set the entry point for the Docker container
ENTRYPOINT ["java", "-jar", "app.jar"]

# Expose the port that your application is listening on
EXPOSE 10001
