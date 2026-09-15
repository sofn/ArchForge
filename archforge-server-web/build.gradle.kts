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
    // Gradle 9 up-to-date checks can skip bootRun; an app must always run.
    outputs.upToDateWhen { false }
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

// Exports the live springdoc OpenAPI document to build/openapi/live-openapi.json.
// Consumed by :archforge-server-admin:generateOpenApi which merges both exports into spec/openapi.yaml.
tasks.register<Test>("exportOpenApi") {
    description = "Exports server-web's live OpenAPI document to build/openapi/live-openapi.json."
    group = "contract"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    filter { includeTestsMatching("com.lesofn.archforge.server.web.contract.OpenApiSnapshotTest") }
    // Declared so a FROM-CACHE hit restores the JSON — without it generateOpenApi
    // reads a file that cache-skipped execution never wrote.
    outputs.file(layout.buildDirectory.file("openapi/live-openapi.json"))
}

tasks.named("collectReachabilityMetadata") {
    enabled = false
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.1.0"))
    implementation(platform(project(":archforge-dependencies")))

    api(project(":archforge-common:archforge-common-base"))
    api(project(":archforge-common:archforge-common-jpa"))
    api(project(":archforge-infrastructure"))
    api(project(":archforge-module-cms"))
    api(project(":archforge-builtin:archforge-admin-user"))

    api("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }

    api("org.springframework.boot:spring-boot-starter-log4j2") {
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }

    // 密码加密（web 端直接使用 BCryptPasswordEncoder，见 PasswordConfig）
    api("org.springframework.security:spring-security-crypto")

    // 邮件发送（真实 SMTP 配置通过 spring.mail.* 在 yml 中开启）
    api("org.springframework.boot:spring-boot-starter-mail") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }

    api("org.springdoc:springdoc-openapi-starter-webmvc-ui")

    developmentOnly("org.springframework.boot:spring-boot-devtools:4.1.0")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    compileOnly(platform(project(":archforge-dependencies")))
    compileOnly("org.mapstruct:mapstruct")
    annotationProcessor(platform(project(":archforge-dependencies")))
    annotationProcessor("org.mapstruct:mapstruct-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // 集成测试容器基座（Testcontainers PG/Redis，DynamicPropertySource 注入数据源）
    // + cms 测试数据 builder
    testImplementation(testFixtures(project(":archforge-common:archforge-common-jpa")))
    testImplementation(testFixtures(project(":archforge-module-cms")))

    // ArchUnit architecture tests (G7)
    testImplementation("com.tngtech.archunit:archunit")
}
