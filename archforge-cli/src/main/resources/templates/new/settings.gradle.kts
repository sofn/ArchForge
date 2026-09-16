rootProject.name = "%NAME%"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

// Flat-prefix scan: every archforge-module-* directory is a business module
// (created via `archforge module new <name>`). No manual include() needed.
rootDir.listFiles()
        .orEmpty()
        .filter { it.isDirectory && it.name.startsWith("archforge-module-") }
        .sortedBy { it.name }
        .forEach { include(it.name) }
