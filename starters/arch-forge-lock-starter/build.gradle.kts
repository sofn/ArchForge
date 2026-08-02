dependencies {
    api(project(":starters:arch-forge-redisson-starter"))
    api("org.springframework.boot:spring-boot-starter-aspectj")
    api("org.springframework.boot:spring-boot-starter-data-redis")
    api("org.slf4j:slf4j-api")

    compileOnly("org.projectlombok:lombok")

    testImplementation("org.testcontainers:testcontainers")
}
