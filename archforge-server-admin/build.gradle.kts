plugins {
    id("org.springframework.boot") version "4.1.0"
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
    jvmArgs(
        "--enable-preview",
        "--enable-native-access=ALL-UNNAMED",
        "-Dfile.encoding=UTF-8",
        "-Dconsole.encoding=UTF-8",
        "-Dsun.jnu.encoding=UTF-8",
        "-Ddefault.client.encoding=UTF-8"
    )
    systemProperty("file.encoding", "UTF-8")
    systemProperty("sun.jnu.encoding", "UTF-8")
}

// Spring AOT processing also needs --enable-preview
// Use 'prod' profile during AOT to skip Testcontainers (no Docker in CI/native builds)
// AOT is disabled for standard build/test lifecycle; enable explicitly when running nativeCompile.
tasks.named<JavaExec>("processAot") {
    enabled = false
    jvmArgs("--enable-preview", "--enable-native-access=ALL-UNNAMED")
    systemProperty("spring.profiles.active", "prod")
}
tasks.named<JavaExec>("processTestAot") {
    enabled = false
    jvmArgs("--enable-preview", "--enable-native-access=ALL-UNNAMED")
    systemProperty("spring.profiles.active", "test")
}
tasks.named("compileAotJava") { enabled = false }
tasks.named("processAotResources") { enabled = false }
tasks.named("aotClasses") { enabled = false }
tasks.named("compileAotTestJava") { enabled = false }
tasks.named("processAotTestResources") { enabled = false }
tasks.named("aotTestClasses") { enabled = false }
tasks.named("collectReachabilityMetadata") { enabled = false }

// Unit / integration tests default to the test profile (Testcontainers + Flyway)
tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "test")
    environment("SPRING_PROFILES_ACTIVE", "test")
}

// GraalVM Native Image 配置
graalvmNative {
    binaries {
        named("main") {
            mainClass.set("com.lesofn.archforge.server.admin.Application")
            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(25))
            })
            // JDK 25 preview features (StructuredTaskScope, ScopedValue, etc.)
            buildArgs.add("--enable-preview")
            sharedLibrary.set(false)
            // Cap native-image heap to avoid OOM on constrained hosts (default is 75% of RAM)
            buildArgs.add("-J-Xmx8g")
        }
    }
}

dependencies {
    // 引入 Spring Boot dependencies BOM
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.1.0"))
    // 引入项目统一版本管理平台
    implementation(platform(project(":archforge-dependencies")))
    
    api(project(":archforge-common:archforge-common-base"))
    api(project(":archforge-common:archforge-common-jpa"))
    api(project(":archforge-infrastructure"))
    api(project(":archforge-domain:archforge-admin-user"))
    api(project(":archforge-domain:archforge-meta-table"))
    api(project(":archforge-domain:archforge-blog"))
    api(project(":archforge-example:archforge-example-task"))

    // 排除logback，使用log4j2
    api("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    api("org.springframework.security:spring-security-crypto")
    
    // 添加log4j2依赖
    api("org.springframework.boot:spring-boot-starter-log4j2") {
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }
    api("org.jolokia:jolokia-core")
    
    // Redis
    api("org.springframework.boot:spring-boot-starter-data-redis")
    
    // Flyway
    api("org.flywaydb:flyway-core")
    api("org.flywaydb:flyway-database-postgresql")
    
    // Oshi (系统监控)
    api("com.github.oshi:oshi-core")

    // Druid monitoring
    api("com.alibaba:druid")

    // Testcontainers PostgreSQL for Dev environment
    api("org.testcontainers:testcontainers")
    api("org.testcontainers:testcontainers-postgresql")
    
    // AWS S3 SDK
    api("software.amazon.awssdk:s3")

    // db-scheduler (JDBC store, clustered, single 'scheduled_tasks' table)
    api("com.github.kagkarlsson:db-scheduler")
    
    // Spring Boot DevTools - 开发环境自动重启和热部署
    developmentOnly("org.springframework.boot:spring-boot-devtools:4.1.0")
    
    // Lombok注解处理器
    annotationProcessor("org.projectlombok:lombok:1.18.46")
    compileOnly("org.projectlombok:lombok")

    // MapStruct注解处理器
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    compileOnly("org.mapstruct:mapstruct:1.6.3")

    // Spring Modulith verification and documentation
    testImplementation("org.springframework.modulith:spring-modulith-test")
    testImplementation("org.springframework.modulith:spring-modulith-docs")

    // ArchUnit architecture tests
    testImplementation("com.tngtech.archunit:archunit")

    // Cross-module test data builders (G2)
    testImplementation(testFixtures(project(":archforge-domain:archforge-admin-user")))
    testImplementation(testFixtures(project(":archforge-domain:archforge-blog")))
    testImplementation(testFixtures(project(":archforge-domain:archforge-meta-table")))
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

