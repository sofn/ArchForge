dependencies {
    api(project(":starters:arch-forge-redisson-starter"))

    api("org.springframework.boot:spring-boot-starter-jackson") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }

    api("com.github.ben-manes.caffeine:caffeine")
    api("org.springframework:spring-context-support")

    compileOnly("org.projectlombok:lombok")

    testImplementation("org.testcontainers:testcontainers")
}
