dependencies {
    api(project(":domain:admin-user-api"))
    implementation(project(":infrastructure"))

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
