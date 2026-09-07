plugins {
    `java-platform`
    `maven-publish`
}

group = "com.lesofn.archforge"
version = "0.1.SNAPSHOT"

// 配置平台，允许定义依赖约束
javaPlatform {
    allowDependencies()
}

dependencies {
    // 定义依赖约束，这些依赖不会被直接引入，但会为使用它们的项目提供版本管理
    constraints {
        // 数据库相关
        api("com.baomidou:dynamic-datasource-spring-boot4-starter:4.5.0")
        api("com.alibaba:druid:1.2.24")
        api("org.postgresql:postgresql:42.7.11")
        
        // 常用工具类
        api("com.google.guava:guava:33.6.0-jre")
        api("commons-io:commons-io:2.22.0")
        api("org.apache.commons:commons-lang3:3.20.0")
        api("commons-codec:commons-codec:1.22.0")
        api("org.apache.commons:commons-collections4:4.5.0")
        
        // 实用工具
        api("org.javatuples:javatuples:1.2")
        
        // HTTP客户端
        api("com.konghq:unirest-java-core:4.10.0")
        
        // Web相关
        api("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

        api("org.jolokia:jolokia-core:1.7.2")
        
        // JWT
        api("io.jsonwebtoken:jjwt-api:0.12.7")
        api("io.jsonwebtoken:jjwt-impl:0.12.7")
        api("io.jsonwebtoken:jjwt-jackson:0.12.7")

        // Sa-Token
        api("cn.dev33:sa-token-spring-boot3-starter:1.45.0")
        api("cn.dev33:sa-token-redis-jackson:1.45.0")
        
        // 其他
        api("com.google.code.findbugs:annotations:3.0.1")
        api("org.lionsoul:ip2region:2.7.0")
        api("eu.bitwalker:UserAgentUtils:1.21")
        api("org.jspecify:jspecify:1.0.0")
        
        // Lombok and SLF4J (versions managed by Spring Boot BOM)
        api("org.projectlombok:lombok:1.18.46")
        api("org.slf4j:slf4j-api:2.0.18")
        api("org.slf4j:slf4j-simple:2.0.18")
        
        // 测试相关 (JUnit 6.x for Spring Boot 4)
        api("org.junit.jupiter:junit-jupiter-api:6.0.3")
        api("org.junit.jupiter:junit-jupiter-engine:6.0.3")
        // Testcontainers
        api("org.testcontainers:testcontainers:2.0.5")
        api("org.testcontainers:testcontainers-junit-jupiter:2.0.5")
        api("org.testcontainers:testcontainers-postgresql:2.0.5")
        // Spock 2.4 with Groovy 5.0
        api("org.spockframework:spock-core:2.4-groovy-5.0")
        api("org.spockframework:spock-spring:2.4-groovy-5.0")
        api("org.apache.groovy:groovy:5.0.6")

        // Kaptcha 验证码
        api("com.github.penggle:kaptcha:2.3.2")

        // MapStruct
        api("org.mapstruct:mapstruct:1.6.3")
        api("org.mapstruct:mapstruct-processor:1.6.3")

        // Micrometer + OpenTelemetry (versions aligned with Spring Boot 4.1.0 BOM)
        api("io.micrometer:micrometer-tracing:1.6.6")
        api("io.micrometer:micrometer-tracing-bridge-otel:1.7.0")
        api("io.micrometer:micrometer-registry-prometheus:1.14.4")
        api("io.opentelemetry:opentelemetry-exporter-otlp:1.62.0")

        // Flyway (versions aligned with Spring Boot 4.1.0 BOM)
        api("org.flywaydb:flyway-core:12.4.0")
        api("org.flywaydb:flyway-database-postgresql:12.4.0")

        // Oshi (系统监控)
        api("com.github.oshi:oshi-core:6.8.1")

        // AWS S3 SDK (文件存储)
        api("software.amazon.awssdk:s3:2.46.8")

        // FastExcel (Excel I/O — 替代 EasyExcel/POI 的高性能 Excel 读写库)
        api("org.dhatim:fastexcel:0.20.2")
        api("org.dhatim:fastexcel-reader:0.20.2")

        // db-scheduler (调度 — 单表 JDBC 存储，集群安全，支持动态持久化 schedule)
        api("com.github.kagkarlsson:db-scheduler:16.12.0")

        // Spring Modulith
        api("org.springframework.modulith:spring-modulith-api:2.1.0")
        api("org.springframework.modulith:spring-modulith-core:2.1.0")
        api("org.springframework.modulith:spring-modulith-test:2.1.0")
        api("org.springframework.modulith:spring-modulith-docs:2.1.0")

        // Redisson (distributed lock + cache sync)
        api("org.redisson:redisson:3.52.0")

        // ArchUnit (architecture testing)
        api("com.tngtech.archunit:archunit:1.4.2")

        // FreeMarker (template engine for code generation)
        api("org.freemarker:freemarker:2.3.33")

        // CLI
        api("info.picocli:picocli:4.7.6")
        api("info.picocli:picocli-shell-jline3:4.7.6")
    }
}

publishing {
    publications {
        create<MavenPublication>("bom") {
            from(components["javaPlatform"])
            artifactId = "archforge-dependencies"
        }
    }
}
