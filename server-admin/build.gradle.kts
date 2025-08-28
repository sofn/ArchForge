plugins {
    id("org.springframework.boot") version "3.5.4"
}

// 构建可执行jar/war包
configurations {
    create("providedRuntime")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}

dependencies {
    // 引入 Spring Boot dependencies BOM
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.4"))
    
    api(project(":common:common-core"))
    api(project(":infrastructure"))
    api(project(":domain:admin-user"))
    api(project(":example:example-task"))

    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.jolokia:jolokia-core")
    
    // JWT
    api("io.jsonwebtoken:jjwt-api")
    api("io.jsonwebtoken:jjwt-impl")
    api("io.jsonwebtoken:jjwt-jackson")
    
    // Redis
    api("org.springframework.boot:spring-boot-starter-data-redis")
    
    // Redis Mock for Dev environment
    api("com.github.fppt:jedis-mock")
    
    // Kaptcha 验证码
    api("com.github.penggle:kaptcha")
    
    // Lombok注解处理器
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    compileOnly("org.projectlombok:lombok")
}

// profile环境配置文件
sourceSets {
    main {
        resources {
            setSrcDirs(listOf("src/main/resources/", "src/main/profiles/${findProperty("profile") ?: "dev"}"))
        }
    }
}
