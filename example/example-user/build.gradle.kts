dependencies {
    api(project(":frame"))
    
    // Lombok
    compileOnly("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
