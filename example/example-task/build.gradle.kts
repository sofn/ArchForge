dependencies {
    api(project(":infrastructure"))
    api(project(":domain:admin-user"))
    
    // Lombok
    compileOnly("org.projectlombok:lombok")
}
