dependencies {
    api(project(":archforge-starters:archforge-redisson-starter"))
    api("com.github.ben-manes.caffeine:caffeine")
    api("org.springframework:spring-context-support")
    api("org.springframework.boot:spring-boot-starter-data-redis")
    api("org.springframework.boot:spring-boot-starter-json")
    api("org.slf4j:slf4j-api")
    compileOnly("org.projectlombok:lombok")

    testImplementation("org.testcontainers:testcontainers")
}
