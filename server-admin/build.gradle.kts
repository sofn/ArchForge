plugins {
    id("org.springframework.boot") version "4.0.5"
    id("org.graalvm.buildtools.native")
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
    enabled = true
}

tasks.jar {
    enabled = true
    archiveClassifier.set("plain")
}

// JDK 25: enable preview features (StructuredTaskScope) + suppress Netty native-access warning
tasks.bootRun {
    jvmArgs("--enable-preview", "--enable-native-access=ALL-UNNAMED")
}

// Spring AOT processing also needs --enable-preview
tasks.named<JavaExec>("processAot") {
    jvmArgs("--enable-preview")
}
tasks.named<JavaExec>("processTestAot") {
    jvmArgs("--enable-preview")
}

// GraalVM Native Image 配置
graalvmNative {
    binaries {
        named("main") {
            mainClass.set("com.lesofn.archsmith.server.admin.Application")
            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(25))
            })
        }
    }
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
    
    // Flyway
    api("org.flywaydb:flyway-core")
    api("org.flywaydb:flyway-database-postgresql")
    
    // Oshi (系统监控)
    api("com.github.oshi:oshi-core")
    
    // Testcontainers PostgreSQL for Dev environment
    api("org.testcontainers:testcontainers")
    api("org.testcontainers:testcontainers-postgresql")
    
    // AWS S3 SDK
    api("software.amazon.awssdk:s3")
    
    // Spring Boot DevTools - 开发环境自动重启和热部署
    developmentOnly("org.springframework.boot:spring-boot-devtools:4.0.5")
    
    // Lombok注解处理器
    annotationProcessor("org.projectlombok:lombok:1.18.44")
    compileOnly("org.projectlombok:lombok")

    // MapStruct注解处理器
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    compileOnly("org.mapstruct:mapstruct:1.6.3")
}

// jlink: 生成最小化 JRE (Spring Boot Web + Actuator + JPA 所需模块)
val buildMinimalJre by tasks.registering(Exec::class) {
    group = "build"
    description = "Builds a minimal JRE using jlink for this Spring Boot application"

    val requiredModules = listOf(
        "java.base",
        "java.compiler",
        "java.desktop",
        "java.instrument",
        "java.management",
        "java.prefs",
        "java.rmi",
        "java.scripting",
        "java.security.jgss",
        "java.sql",
        "jdk.jfr",
        "jdk.unsupported",
        "jdk.crypto.ec",
        "jdk.management",
        "jdk.management.agent"
    ).joinToString(",")

    val javaHome = System.getProperty("java.home")
    val jreOutputDir = layout.buildDirectory.dir("minimal-jre").get().asFile

    inputs.property("modules", requiredModules)
    outputs.dir(jreOutputDir)

    doFirst {
        jreOutputDir.deleteRecursively()
    }

    commandLine(
        "$javaHome/bin/jlink",
        "--add-modules", requiredModules,
        "--strip-debug",
        "--no-man-pages",
        "--no-header-files",
        "--compress", "zip-6",
        "--output", jreOutputDir.absolutePath
    )
}

