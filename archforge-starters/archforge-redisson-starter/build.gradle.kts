dependencies {
    api("org.springframework.boot:spring-boot-starter-data-redis")
    api("org.redisson:redisson")
    api("org.slf4j:slf4j-api")
    // RedisJsonSerializers only touches Jackson 3 when a caller uses it — the consumers (server apps, cache starter)
    // already ship it, so it is not forced onto every Redisson-only user.
    compileOnly("org.springframework.boot:spring-boot-starter-json")

    testImplementation("org.springframework.boot:spring-boot-starter-json")
    testImplementation("org.testcontainers:testcontainers")
}
