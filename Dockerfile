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

# Accept Android SDK licenses
RUN yes | ${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager --licenses

# Copy the Android app to the container
COPY . /app
WORKDIR /app

# Build the Android app
RUN ./gradlew assembleRelease

# Set the entrypoint command to start the app
CMD ["./gradlew", "installRelease"]
