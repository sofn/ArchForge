# ---- JVM multi-stage build ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN ./gradlew :server-admin:bootJar -x test --no-daemon

# ---- Runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /workspace/server-admin/build/libs/server-admin-*-SNAPSHOT.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE="prod"

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
