# Base image with necessary dependencies
FROM adoptopenjdk:11-jdk-hotspot

# Copy the Android app to the container
COPY . /app
WORKDIR /app
