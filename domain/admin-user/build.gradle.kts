dependencies {
    api(project(":infrastructure"))

    // Spring Data JPA
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    // Spring Security
    api("org.springframework.boot:spring-boot-starter-security")

    // QueryDSL
    api("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    api("com.querydsl:querydsl-apt:5.1.0:jakarta")
    api("jakarta.persistence:jakarta.persistence-api:3.2.0")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok:1.18.44")
    
    // QueryDSL APT processor for JPA
    annotationProcessor("com.querydsl:querydsl-apt:5.1.0:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.2.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// 清理任务
tasks.clean {
    doLast {
        delete("src/main/generated")
    }
}
