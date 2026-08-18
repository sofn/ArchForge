plugins {
    `java-library`
}

dependencies {
    api(project(":archforge-infrastructure"))
    api(project(":archforge-domain:archforge-meta-table"))

    // Lombok
    compileOnly("org.projectlombok:lombok")

    // Testcontainers
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
}
