pluginManagement {
    repositories {
        // Prefer local mirrors for plugin resolution, then fall back to official sources
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin/") }
        maven { url = uri("https://maven.aliyun.com/repository/public/") }
        maven { url = uri("https://maven.aliyun.com/repository/spring/") }
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("org.graalvm.buildtools.native") version "0.11.5"
    }
    // Ensure Spring Boot plugin can be resolved even if the plugin marker isn't available on the portal
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.springframework.boot") {
                useModule("org.springframework.boot:spring-boot-gradle-plugin:${requested.version}")
            }
        }
    }
}

rootProject.name = "ArchForge"

include("common:common-base")
include("common:common-jpa")
include("common:common-error")
include("infrastructure")
include("dependencies")
include("server-admin")

include("domain:admin-user")
include("domain:meta-table")

file("example").listFiles()?.filter {
    it.isDirectory && File(it, "build.gradle.kts").exists()
}?.forEach { dir ->
    include("example:${dir.name}")
}

include("starters:arch-forge-redisson-starter")
include("starters:arch-forge-cache-starter")
include("starters:arch-forge-lock-starter")
include("starters:arch-forge-trace-starter")

// Configure build file names for subprojects
rootProject.children.forEach { project ->
    // All subprojects now use build.gradle.kts
    project.buildFileName = "build.gradle.kts"
    
    require(project.projectDir.isDirectory) { "Project directory must exist: ${project.projectDir}" }
}
