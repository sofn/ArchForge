dependencies {
    api("com.google.guava:guava")
    api("org.apache.commons:commons-lang3")
    compileOnly("org.springframework:spring-web")
    compileOnly("org.springframework:spring-context")
    compileOnly("org.springframework:spring-webmvc")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    
    // Lombok
    compileOnly("org.projectlombok:lombok")
    
    // SLF4J
    implementation("org.slf4j:slf4j-api")
    
    // JUnit for tests
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    
    // Lombok for tests
    testCompileOnly("org.projectlombok:lombok")
}
