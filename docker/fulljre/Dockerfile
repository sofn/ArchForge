# ---- Traditional JVM Deployment (Full JRE) ----
# Pre-requisite: ./gradlew :server-admin:bootJar -x test
# Build with: docker build -f docker/fulljre/Dockerfile -t archforge:fulljre .
#
# Simplest deployment mode — uses the complete Azul Zulu JRE.
# Larger image (~500MB) but zero build complexity, full JDK tooling available.
FROM azul/zulu-openjdk-alpine:25-jre
WORKDIR /app

RUN addgroup -g 1001 archforge && adduser -u 1001 -G archforge -D archforge \
    && mkdir -p /app/logs && chown archforge:archforge /app/logs
USER archforge

ARG JAR_FILE=server-admin/build/libs/server-admin.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseZGC \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    --enable-preview \
    --enable-native-access=ALL-UNNAMED \
    -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE="prod"

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
