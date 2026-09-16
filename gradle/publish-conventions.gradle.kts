// Shared Maven publication for the ArchForge release set (plan T6 / P1-C).
// Applied to release-set members only — see `releaseModules` in the root
// build.gradle.kts. Not applied to designer / cli / server-* / module-*.
//
// Coordinates: com.lesofn.archforge:<artifactId>:<archforgeVersion>
// (group/version inherit from the root project; artifactId = project name).
//
// testFixtures note: modules using `java-test-fixtures` publish fixtures as a
// `-test-fixtures` classifier artifact plus a Gradle Module Metadata variant —
// fixture code never enters the main jar, and consumers must opt in via
// `testImplementation(testFixtures("…"))`. The BOM (archforge-dependencies)
// publishes its own javaPlatform publication and is not wired through here.

import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

apply(plugin = "maven-publish")

configure<JavaPluginExtension> {
    withSourcesJar()
    withJavadocJar()
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
