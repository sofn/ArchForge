dependencies {
    api(project(":starters:arch-forge-redisson-starter"))

    api("org.springframework.boot:spring-boot-starter-aspectj") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }

    compileOnly("org.projectlombok:lombok")

    testImplementation("org.testcontainers:testcontainers")
}
