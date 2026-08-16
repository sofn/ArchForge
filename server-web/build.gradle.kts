plugins {
    id("org.springframework.boot") version "4.1.0"
    id("org.graalvm.buildtools.native")
}

configurations {
    create("providedRuntime")

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

tasks.named("compileAotJava") {
    enabled = false
}

tasks.named("processAotResources") {
    enabled = false
}

tasks.named("aotClasses") {
    enabled = false
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "test")
    environment("SPRING_PROFILES_ACTIVE", "test")
}

tasks.named("collectReachabilityMetadata") {
    enabled = false
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.1.0"))
    implementation(platform(project(":dependencies")))

    api(project(":common:common-base"))
    api(project(":common:common-jpa"))
    api(project(":infrastructure"))
    api(project(":domain:blog"))
    api(project(":domain:admin-user"))

    api("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }

    api("org.springframework.boot:spring-boot-starter-log4j2") {
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }

    // Sa-Token 认证与 Redis 会话存储
    api("cn.dev33:sa-token-spring-boot3-starter")
    api("cn.dev33:sa-token-redis-jackson")

    // 密码加密（复用 domain:admin-user 的 PasswordEncoderPort）
    api("org.springframework.security:spring-security-crypto")

    // 邮件发送（真实 SMTP 配置通过 spring.mail.* 在 yml 中开启）
    api("org.springframework.boot:spring-boot-starter-mail") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }

    api("org.springdoc:springdoc-openapi-starter-webmvc-ui")

    developmentOnly("org.springframework.boot:spring-boot-devtools:4.1.0")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    compileOnly(platform(project(":dependencies")))
    compileOnly("org.mapstruct:mapstruct")
    annotationProcessor(platform(project(":dependencies")))
    annotationProcessor("org.mapstruct:mapstruct-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
