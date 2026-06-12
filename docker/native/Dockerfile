# ---- Native Image Build Stage (BellSoft Liberica NIK 25) ----
# Build with: docker build --network=host -f docker/native/Dockerfile -t archforge:native .
FROM bellsoft/liberica-native-image-kit-container:jdk-25-nik-25-stream-glibc AS build
WORKDIR /workspace
COPY . .
RUN ./gradlew :server-admin:nativeCompile -x test -x spotlessCheck --no-daemon

# ---- Runtime Stage (minimal) ----
FROM debian:trixie-slim
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    libz-dev libstdc++6 curl \
    && rm -rf /var/lib/apt/lists/*

# 安全: 使用非 root 用户运行
RUN groupadd -g 1001 archforge && useradd -u 1001 -g archforge -m archforge \
    && mkdir -p /app/logs && chown archforge:archforge /app/logs
USER archforge

COPY --from=build /workspace/server-admin/build/native/nativeCompile/server-admin app

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE="prod"

ENTRYPOINT ["./app"]
