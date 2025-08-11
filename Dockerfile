# Build Stage (Maven + Java 21)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Cache dependencies first (improves build speed)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build the application
COPY src ./src
RUN mvn clean package -DskipTests -P prod -B

# Verify JAR file exists (debugging)
RUN ls -la /app/target/

# Runtime Stage (Lightweight JRE)
FROM eclipse-temurin:21-jre-alpine

# Security: Non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Install curl for health checks (optional)
USER root
RUN apk add --no-cache curl
USER spring

WORKDIR /app

# Copy JAR from build stage
COPY --from=build --chown=spring:spring /app/target/*.jar auther-service.jar


# Run Eureka
EXPOSE 10001
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar auther-service.jar --spring.profiles.active=prod"]