// Java version inherited from parent build.gradle.kts (Java 17)

dependencies {
    // 依赖 common-core 模块
    api(project(":common-core"))
    
    // 核心框架依赖 (Spring Boot BOM 管理的版本)
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-aop")
    api("org.springframework.boot:spring-boot-starter-actuator")
    
    // 日志依赖 (Spring Boot BOM 管理的版本)
    api("org.slf4j:slf4j-api")
    
    // Lombok
    compileOnly("org.projectlombok:lombok")

    // JUnit for tests
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}