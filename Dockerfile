# Base image with necessary dependencies
FROM adoptopenjdk:11-jdk-hotspot

# Set environment variables
ENV ANDROID_SDK_ROOT=/opt/android-sdk-linux
ENV ANDROID_HOME=$ANDROID_SDK_ROOT
ENV PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools

# Install required packages
RUN apt-get --quiet update && \
    apt-get --quiet install --yes --no-install-recommends wget unzip lib32stdc++6 lib32z1 && \
    apt-get --quiet clean && \
    rm -rf /var/lib/apt/lists/*

# Download and install Android SDK Command Line Tools
RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools && \
    wget --quiet --output-document=commandlinetools.zip https://dl.google.com/android/repository/commandlinetools-linux-7583922_latest.zip && \
    unzip -q commandlinetools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools && \
    rm commandlinetools.zip

# Install Android SDK components
RUN echo "y" | ${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager "platform-tools" "build-tools;30.0.3" "platforms;android-30"

# Copy the Android app to the container
COPY . /app
WORKDIR /app

# Adjust permissions of Gradle wrapper files
RUN chmod +x ./gradlew

# Build the Android app
RUN ./gradlew assembleRelease --stacktrace

# Set the entrypoint command to start the app
CMD ["./gradlew", "installRelease"]
