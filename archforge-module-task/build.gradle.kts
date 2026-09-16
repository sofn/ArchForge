dependencies {
    implementation("org.springframework.boot:spring-boot-starter-validation")
    api(project(":archforge-infrastructure"))
    
    // Lombok
    compileOnly("org.projectlombok:lombok")
}
