dependencies {
    api("org.springframework.boot:spring-boot-starter-data-redis") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }
    api("org.redisson:redisson")

    testImplementation("org.testcontainers:testcontainers")
}
