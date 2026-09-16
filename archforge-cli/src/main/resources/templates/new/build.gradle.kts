plugins {
    java
    id("org.springframework.boot") version "4.1.0"
}

group = "%GROUP%"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

allprojects {
    repositories {
        // SNAPSHOT builds of the ArchForge release set resolve here first.
        mavenLocal()
        mavenCentral()
    }
}

// Business modules (archforge-module-*) get the platform constraints injected
// here so their own build files stay version-free. annotationProcessor does not
// inherit implementation — the platform must be attached there too.
subprojects {
    apply(plugin = "java-library")
    dependencies {
        "api"(platform("org.springframework.boot:spring-boot-dependencies:4.1.0"))
        "api"(platform("com.lesofn.archforge:archforge-dependencies:${property("archforgeVersion")}"))
        "annotationProcessor"(platform("com.lesofn.archforge:archforge-dependencies:${property("archforgeVersion")}"))
        "testAnnotationProcessor"(platform("com.lesofn.archforge:archforge-dependencies:${property("archforgeVersion")}"))
    }
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.1.0"))
    implementation(platform("com.lesofn.archforge:archforge-dependencies:${property("archforgeVersion")}"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // ArchForge release set — versions come from the BOM above.
    implementation("com.lesofn.archforge:archforge-common-base")
    implementation("com.lesofn.archforge:archforge-common-error")
    implementation("com.lesofn.archforge:archforge-request-log-starter")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
