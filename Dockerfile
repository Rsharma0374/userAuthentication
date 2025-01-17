## Use an official OpenJDK runtime as a base image
#FROM openjdk:17-jdk-slim
#
## Set the working directory inside the container
#WORKDIR /userAuthentication
#
## Copy the JAR file (or build the app if using source code)
#COPY target/*.jar userAuthentication.jar
#
## Create the logs directory inside the container
#RUN mkdir -p /opt/logs && chmod 755 /opt/logs
#
## Expose the port your app will run on
#EXPOSE 10001
#
## Define the command to run the app
#ENTRYPOINT ["java", "-jar", "userAuthentication.jar"]


# Use an official Maven image to build the application
FROM maven:3.8.8-eclipse-temurin-17 AS build

# Set the working directory for the build
WORKDIR /app

# Copy the Maven project files to the container
COPY pom.xml ./
COPY src ./src
COPY /opt/configs/emailConnector.properties /opt/configs/emailConnector.properties
RUN chmod 755 /opt/configs/emailConnector.properties

# Run the Maven build with the production profile
RUN mvn clean install -P prod

# Use an official OpenJDK runtime as the base image for the final build
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /userAuthentication

# Copy the JAR file from the Maven build stage
COPY --from=build /app/target/*.jar userAuthentication.jar

# Create the logs directory inside the container
RUN mkdir -p /opt/logs && chmod 755 /opt/logs

# Expose the port your app will run on
EXPOSE 10001

# Define the command to run the app
ENTRYPOINT ["java", "-jar", "userAuthentication.jar"]
