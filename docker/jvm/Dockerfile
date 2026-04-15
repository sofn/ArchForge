# ---- JVM Build Stage (Project Leyden / CDS optimized) ----
# 先在宿主机上执行: ./gradlew :server-admin:bootJar -x test
# 然后使用此 Dockerfile 构建运行时镜像

FROM azul/zulu-openjdk:25 AS training
WORKDIR /app
ARG JAR_FILE=server-admin/build/libs/server-admin.jar
COPY ${JAR_FILE} app.jar

# 生成 CDS 归档 (Project Leyden AOT 缓存)
RUN java -XX:AOTMode=record -XX:AOTConfiguration=app.aotconf \
    -Dspring.context.exit=onRefresh \
    -jar app.jar || true
RUN java -XX:AOTMode=create -XX:AOTConfiguration=app.aotconf \
    -XX:AOTCache=app.aot \
    -jar app.jar || true

# ---- Runtime Stage ----
FROM azul/zulu-openjdk-alpine:25-jre
WORKDIR /app

# 安全: 使用非 root 用户运行
RUN addgroup -g 1001 archsmith && adduser -u 1001 -G archsmith -D archsmith
USER archsmith

COPY --from=training /app/app.jar app.jar
COPY --from=training /app/app.aot app.aot

EXPOSE 8080

ENV JAVA_OPTS="-XX:AOTCache=app.aot \
    -XX:+UseCompactObjectHeaders \
    -XX:+UseZGC \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    --enable-native-access=ALL-UNNAMED \
    -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE="prod"

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
