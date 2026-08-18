# ---- JVM Build Stage (Project Leyden CDS + jlink minimal JRE) ----
# Pre-requisite: ./gradlew :archforge-server-admin:bootJar -x test
# Build with: docker build -f docker/jlink/Dockerfile -t archforge:jlink .
FROM azul/zulu-openjdk:25 AS training
WORKDIR /app

# binutils provides objcopy (needed by jlink --strip-debug)
RUN apt-get update && apt-get install -y --no-install-recommends binutils && rm -rf /var/lib/apt/lists/*

ARG JAR_FILE=archforge-server-admin/build/libs/archforge-server-admin.jar
COPY ${JAR_FILE} app.jar

# Generate CDS archive (Project Leyden AOT cache)
RUN java --enable-preview -XX:AOTMode=record -XX:AOTConfiguration=app.aotconf \
    -Dspring.context.exit=onRefresh \
    -Dspring.profiles.active=prod \
    -jar app.jar || true
RUN java --enable-preview -XX:AOTMode=create -XX:AOTConfiguration=app.aotconf \
    -XX:AOTCache=app.aot \
    -jar app.jar || true

# jlink: build minimal JRE (only modules needed by Spring Boot)
RUN jlink \
    --add-modules java.base,java.compiler,java.desktop,java.instrument,java.management,java.prefs,java.rmi,java.scripting,java.security.jgss,java.sql,jdk.jfr,jdk.unsupported,jdk.crypto.ec,jdk.management,jdk.management.agent \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress zip-6 \
    --output /app/minimal-jre

# ---- Runtime Stage (minimal JRE from jlink) ----
FROM debian:trixie-slim
WORKDIR /app

RUN groupadd -g 1001 archforge && useradd -u 1001 -g archforge -m archforge \
    && mkdir -p /app/logs && chown archforge:archforge /app/logs
USER archforge

COPY --from=training /app/minimal-jre /app/jre
COPY --from=training /app/app.jar app.jar
COPY --from=training /app/app.aot app.aot

EXPOSE 8080

ENV JAVA_HOME="/app/jre"
ENV PATH="${JAVA_HOME}/bin:${PATH}"
ENV JAVA_OPTS="-XX:AOTCache=app.aot \
    -XX:+UseCompactObjectHeaders \
    -XX:+UseZGC \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    --enable-preview \
    --enable-native-access=ALL-UNNAMED \
    -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE="prod"

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
