// Java 21

dependencies {
    // 依赖 common 基础模块
    api(project(":common:common-base"))
    api(project(":common:common-jpa"))
    
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
    api("org.springframework.boot:spring-boot-starter-actuator") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }
    api("org.springframework.boot:spring-boot-starter-security") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }
    api("org.springframework.boot:spring-boot-starter-data-redis") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }

    // Caffeine L1 cache + Spring context support for CaffeineCacheManager
    api("com.github.ben-manes.caffeine:caffeine")
    api("org.springframework:spring-context-support")

    // Redisson (distributed lock + cache sync)
    api("org.redisson:redisson")

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

    // Micrometer + OpenTelemetry
    api("io.micrometer:micrometer-tracing")
    api("io.micrometer:micrometer-tracing-bridge-otel")
    api("io.micrometer:micrometer-registry-prometheus")
    api("io.opentelemetry:opentelemetry-exporter-otlp")

    // AWS S3 SDK (文件存储)
    api("software.amazon.awssdk:s3")

    // JUnit for tests
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
