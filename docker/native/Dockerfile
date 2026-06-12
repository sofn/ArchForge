# ---- Native Image Build Stage (BellSoft Liberica NIK 25) ----
# Build with: docker build --network=host -f docker/native/Dockerfile -t archforge:native .
# Requires: 8GB+ RAM for native-image compilation
FROM bellsoft/liberica-native-image-kit-container:jdk-25-nik-25-stream-glibc AS build
WORKDIR /workspace
COPY . .
# Limit Gradle/AOT JVM memory to leave headroom for native-image (needs ~6GB)
ENV GRADLE_OPTS="-Xmx512m" JAVA_TOOL_OPTIONS="-Xmx1g"
RUN ./gradlew :server-admin:nativeCompile -x test -x spotlessCheck --no-daemon -Dorg.gradle.jvmargs="-Xmx1g"

# ---- Runtime Stage ----
FROM debian:trixie-slim
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    libz-dev libstdc++6 curl \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd -g 1001 archforge && useradd -u 1001 -g archforge -m archforge \
    && mkdir -p /app/logs && chown archforge:archforge /app/logs
USER archforge

COPY --from=build /workspace/server-admin/build/native/nativeCompile/server-admin app

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE="prod"

ENTRYPOINT ["./app"]
