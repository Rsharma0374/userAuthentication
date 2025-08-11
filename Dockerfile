# Build Stage (Maven + Java 21)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Step 1: Copy only pom.xml and download dependencies (cached if pom.xml unchanged)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Step 2: Copy actual source code
COPY src ./src

# Step 3: Build the application
RUN mvn clean package -DskipTests -P prod -B

# Runtime Stage (Lightweight JRE)
FROM eclipse-temurin:21-jre-alpine

# Security: Non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Install curl for health checks
USER root
RUN apk add --no-cache curl
USER spring

WORKDIR /app

# Copy JAR from build stage
COPY --from=build --chown=spring:spring /app/target/*.jar auth-service.jar

# Run app
EXPOSE 10001
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar auth-service.jar --spring.profiles.active=prod"]
