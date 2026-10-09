dependencies {
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation(project(":archforge-infrastructure"))
    
    // Lombok
    compileOnly("org.projectlombok:lombok")
}
