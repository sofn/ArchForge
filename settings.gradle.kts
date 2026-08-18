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

include("archforge-common:archforge-common-base")
include("archforge-common:archforge-common-jpa")
include("archforge-common:archforge-common-error")
include("archforge-infrastructure")
include("archforge-dependencies")
include("archforge-server-admin")
include("archforge-domain:archforge-blog")
include("archforge-server-web")

include("archforge-domain:archforge-admin-user")
include("archforge-domain:archforge-meta-table")

file("archforge-example").listFiles()?.filter {
    it.isDirectory && File(it, "build.gradle.kts").exists()
}?.forEach { dir ->
    include("archforge-example:${dir.name}")
}

include("archforge-starters:archforge-redisson-starter")
include("archforge-starters:archforge-cache-starter")
include("archforge-starters:archforge-lock-starter")
include("archforge-starters:archforge-trace-starter")

// Configure build file names for subprojects
rootProject.children.forEach { project ->
    // All subprojects now use build.gradle.kts
    project.buildFileName = "build.gradle.kts"

    require(project.projectDir.isDirectory) { "Project directory must exist: ${project.projectDir}" }
}
