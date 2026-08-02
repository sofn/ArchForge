dependencies {
    api("org.springframework.boot:spring-boot-starter-data-redis")
    api("org.redisson:redisson")
    api("org.slf4j:slf4j-api")

    testImplementation("org.testcontainers:testcontainers")
}
