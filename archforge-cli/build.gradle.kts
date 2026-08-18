plugins {
    id("com.gradleup.shadow") version "8.3.5"
}

group = "com.lesofn.archforge"
version = "0.1.0-SNAPSHOT"

dependencies {
    implementation(project(":archforge-common:archforge-common-base"))
    implementation(project(":archforge-common:archforge-common-error"))

    implementation("info.picocli:picocli:4.7.6")
    implementation("info.picocli:picocli-shell-jline3:4.7.6")
    implementation("tools.jackson.dataformat:jackson-dataformat-yaml")
    implementation("org.slf4j:slf4j-api")
    implementation("org.slf4j:slf4j-simple")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}

tasks.shadowJar {
    archiveBaseName.set("archforge-cli")
    archiveClassifier.set("")
    archiveVersion.set("")
    mergeServiceFiles()
    manifest {
        attributes["Main-Class"] = "com.lesofn.archforge.cli.ArchForgeCli"
    }
}

tasks.named("jar") {
    enabled = false
}

tasks.named("build") {
    dependsOn(tasks.shadowJar)
}
