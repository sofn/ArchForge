dependencies {
    api(project(":archforge-builtin:archforge-meta-runtime"))

    implementation("org.freemarker:freemarker")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    compileOnly(platform(project(":archforge-dependencies")))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(testFixtures(project(":archforge-builtin:archforge-meta-runtime")))
}
