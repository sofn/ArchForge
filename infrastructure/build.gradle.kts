// Java 21

dependencies {
    // 依赖 common 基础模块与可复用 starter
    api(project(":common:common-base"))
    api(project(":common:common-jpa"))
    api(project(":starters:arch-forge-redisson-starter"))
    api(project(":starters:arch-forge-trace-starter"))

    // 核心框架依赖 (Spring Boot BOM 管理的版本)
    // Excluding spring-boot-starter-logging to avoid SLF4J multiple providers issue
    api("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }
    // AspectJ support (replaces spring-boot-starter-aop in Spring Boot 4)
    api("org.springframework.boot:spring-boot-starter-aspectj") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }
    api("org.springframework.boot:spring-boot-starter-security") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }

    // OpenAPI / Swagger
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui")

    // Kaptcha 验证码
    api("com.github.penggle:kaptcha")

    // 日志依赖 (Spring Boot BOM 管理的版本)
    // Using Log4j2 to avoid SLF4J multiple providers issue
    api("org.slf4j:slf4j-api")
    api("org.springframework.boot:spring-boot-starter-log4j2") {
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }

    // Lombok
    compileOnly("org.projectlombok:lombok")

    // AWS S3 SDK (文件存储)
    api("software.amazon.awssdk:s3")

    // sa-token shared by admin and web
    api("cn.dev33:sa-token-spring-boot3-starter")
    api("cn.dev33:sa-token-redis-jackson")

    // JUnit for tests
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
