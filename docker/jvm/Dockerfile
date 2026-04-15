# ---- JVM Build Stage (Project Leyden / CDS + jlink optimized) ----
# 先在宿主机上执行: ./gradlew :server-admin:bootJar -x test
# 然后使用此 Dockerfile 构建运行时镜像

FROM azul/zulu-openjdk:25 AS training
WORKDIR /app
ARG JAR_FILE=server-admin/build/libs/server-admin.jar
COPY ${JAR_FILE} app.jar

# 生成 CDS 归档 (Project Leyden AOT 缓存)
RUN java --enable-preview -XX:AOTMode=record -XX:AOTConfiguration=app.aotconf \
    -Dspring.context.exit=onRefresh \
    -jar app.jar || true
RUN java --enable-preview -XX:AOTMode=create -XX:AOTConfiguration=app.aotconf \
    -XX:AOTCache=app.aot \
    -jar app.jar || true

# jlink: 生成最小化 JRE (仅包含 Spring Boot 所需模块)
RUN jlink \
    --add-modules java.base,java.compiler,java.desktop,java.instrument,java.management,java.prefs,java.rmi,java.scripting,java.security.jgss,java.sql,jdk.jfr,jdk.unsupported,jdk.crypto.ec,jdk.management,jdk.management.agent \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress zip-6 \
    --output /app/minimal-jre

# ---- Runtime Stage (minimal JRE from jlink) ----
FROM alpine:3.21
WORKDIR /app

# 安装 glibc (Zulu JDK 需要)
RUN apk add --no-cache gcompat libstdc++

# 安全: 使用非 root 用户运行
RUN addgroup -g 1001 archsmith && adduser -u 1001 -G archsmith -D archsmith
USER archsmith

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
