# Base image with necessary dependencies
FROM adoptopenjdk:11-jdk-hotspot

# Copy the Android app to the container
COPY . /app
WORKDIR /app

# Adjust permissions of Gradle wrapper files
RUN chmod +x ./gradlew

# Build the Android app
RUN ./gradlew assembleRelease

# Set the entrypoint command to start the app
CMD ["./gradlew", "installRelease"]
