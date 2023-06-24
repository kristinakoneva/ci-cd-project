# Base image with necessary dependencies
FROM adoptopenjdk:11-jdk-hotspot

# Set environment variables
ENV ANDROID_SDK_ROOT=/opt/android-sdk-linux
ENV ANDROID_HOME=$ANDROID_SDK_ROOT
ENV PATH=$PATH:$ANDROID_SDK_ROOT/tools/bin:$ANDROID_SDK_ROOT/platform-tools

# Install required packages
RUN apt-get --quiet update && \
    apt-get --quiet install --yes --no-install-recommends wget tar unzip lib32stdc++6 lib32z1 && \
    apt-get --quiet clean && \
    rm -rf /var/lib/apt/lists/*

# Download and install Android SDK
RUN wget --quiet --output-document=commandlinetools.zip https://dl.google.com/android/repository/commandlinetools-linux-7583922_latest.zip && \
    unzip -q commandlinetools.zip -d commandlinetools && \
    rm commandlinetools.zip

# Install Android SDK components
RUN yes | commandlinetools/tools/bin/sdkmanager --licenses && \
    commandlinetools/tools/bin/sdkmanager --update && \
    commandlinetools/tools/bin/sdkmanager "platform-tools" "build-tools;30.0.3" "platforms;android-30"

# Copy the Android app to the container
COPY . /app
WORKDIR /app

# Build the Android app
RUN ./gradlew assembleRelease

# Set the entrypoint command to start the app
CMD ["./gradlew", "installRelease"]
