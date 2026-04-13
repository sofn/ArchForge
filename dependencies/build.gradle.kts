plugins {
    `java-platform`
    `maven-publish`
}

group = "com.lesofn.appforge"
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
        api("com.mysql:mysql-connector-j:9.3.0")
        api("com.h2database:h2:2.4.240")
        
        // 常用工具类
        api("com.google.guava:guava:33.4.8-jre")
        api("commons-io:commons-io:2.19.0")
        api("org.apache.commons:commons-lang3:3.20.0")
        api("commons-codec:commons-codec:1.18.0")
        api("org.apache.commons:commons-collections4:4.5.0")
        
        // 实用工具
        api("org.javatuples:javatuples:1.2")
        
        // HTTP客户端
        api("com.konghq:unirest-java-core:4.4.7")
        
        // Web相关
        api("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.11")

        api("org.jolokia:jolokia-core:1.7.2")
        
        // JWT
        api("io.jsonwebtoken:jjwt-api:0.12.6")
        api("io.jsonwebtoken:jjwt-impl:0.12.6")
        api("io.jsonwebtoken:jjwt-jackson:0.12.6")
        
        // QueryDSL
        api("com.querydsl:querydsl-jpa:5.1.0")
        api("com.querydsl:querydsl-apt:5.1.0")
        api("jakarta.persistence:jakarta.persistence-api:3.2.0")
        
        // 其他
        api("com.google.code.findbugs:annotations:3.0.1")
        api("org.lionsoul:ip2region:2.7.0")
        api("eu.bitwalker:UserAgentUtils:1.21")
        
        // Lombok and SLF4J (versions managed by Spring Boot BOM)
        api("org.projectlombok:lombok:1.18.44")
        api("org.slf4j:slf4j-api:2.0.17")
        api("org.slf4j:slf4j-simple:2.0.17")
        
        // 测试相关 (JUnit 6.x for Spring Boot 4)
        api("org.junit.jupiter:junit-jupiter-api:6.0.3")
        api("org.junit.jupiter:junit-jupiter-engine:6.0.3")
        api("org.testcontainers:testcontainers:2.0.4")
        api("org.testcontainers:junit-jupiter:2.0.4")
        api("org.testcontainers:mysql:2.0.4")
        // Spock 2.4 with Groovy 5.0
        api("org.spockframework:spock-core:2.4-groovy-5.0")
        api("org.spockframework:spock-spring:2.4-groovy-5.0")
        api("org.apache.groovy:groovy:5.0.5")

        // Redis Mock for Dev environment
        api("com.github.fppt:jedis-mock:1.1.11")
        
        // Kaptcha 验证码
        api("com.github.penggle:kaptcha:2.3.2")

        // MapStruct
        api("org.mapstruct:mapstruct:1.6.3")
        api("org.mapstruct:mapstruct-processor:1.6.3")

        // Micrometer + OpenTelemetry
        api("io.micrometer:micrometer-tracing-bridge-otel:1.5.6")
        api("io.opentelemetry:opentelemetry-exporter-otlp:1.52.0")

    }
}

publishing {
    publications {
        create<MavenPublication>("bom") {
            from(components["javaPlatform"])
            artifactId = "appforge-dependencies"
        }
    }
}
