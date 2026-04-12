pluginManagement {
    repositories {
        // Prefer local mirrors for plugin resolution, then fall back to official sources
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin/") }
        maven { url = uri("https://maven.aliyun.com/repository/public/") }
        maven { url = uri("https://maven.aliyun.com/repository/spring/") }
        gradlePluginPortal()
        mavenCentral()
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

rootProject.name = "AppForge"

include("common:common-core")
include("common:common-error")
include("infrastructure")
include("dependencies")
include("server-admin")

include("domain:admin-user")

include("example:example-task")

// Configure build file names for subprojects
rootProject.children.forEach { project ->
    // All subprojects now use build.gradle.kts
    project.buildFileName = "build.gradle.kts"
    
    require(project.projectDir.isDirectory) { "Project directory must exist: ${project.projectDir}" }
}
