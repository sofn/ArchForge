plugins {
    id("org.springframework.boot") version "4.0.5"
}

// 构建可执行jar/war包
configurations {
    create("providedRuntime")
    
    // 强制排除log4j-to-slf4j和logback依赖
    all {
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "ch.qos.logback", module = "logback-core")
    }
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}

dependencies {
    // 引入 Spring Boot dependencies BOM
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.5"))
    // 引入项目统一版本管理平台
    implementation(platform(project(":dependencies")))
    
    api(project(":common:common-core"))
    api(project(":infrastructure"))
    api(project(":domain:admin-user"))
    api(project(":example:example-task"))

    // 排除logback，使用log4j2
    api("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    api("org.springframework.boot:spring-boot-starter-security") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    
    // 添加log4j2依赖
    api("org.springframework.boot:spring-boot-starter-log4j2") {
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }
    api("org.jolokia:jolokia-core")
    
    // JWT
    api("io.jsonwebtoken:jjwt-api")
    api("io.jsonwebtoken:jjwt-impl")
    api("io.jsonwebtoken:jjwt-jackson")
    
    // Redis
    api("org.springframework.boot:spring-boot-starter-data-redis")
    
    // Redis Mock for Dev environment
    api("com.github.fppt:jedis-mock")
    
    // Druid connection pool and monitoring
    api("com.alibaba:druid-spring-boot-3-starter")
    
    // Spring Boot DevTools - 开发环境自动重启和热部署
    developmentOnly("org.springframework.boot:spring-boot-devtools:4.0.5")
    
    // Lombok注解处理器
    annotationProcessor("org.projectlombok:lombok:1.18.44")
    compileOnly("org.projectlombok:lombok")

    // MapStruct注解处理器
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    compileOnly("org.mapstruct:mapstruct:1.6.3")
}

// profile环境配置文件
sourceSets {
    main {
        resources {
            setSrcDirs(listOf("src/main/resources/", "src/main/profiles/${findProperty("profile") ?: "dev"}"))
        }
    }
}
