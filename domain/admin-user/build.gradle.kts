dependencies {
    api(project(":infrastructure"))

    // Spring Data JPA
    api("org.springframework.boot:spring-boot-starter-data-jpa") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }

    // Spring Security
    api("org.springframework.boot:spring-boot-starter-security") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }

    // QueryDSL
    api("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    api("com.querydsl:querydsl-apt:5.1.0:jakarta")
    api("jakarta.persistence:jakarta.persistence-api:3.1.0")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    
    // QueryDSL APT processor for JPA
    annotationProcessor("com.querydsl:querydsl-apt:5.1.0:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.1.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
}

// QueryDSL configuration
tasks.withType<JavaCompile> {
    options.apply {
        compilerArgs.addAll(listOf(
            "-Aquerydsl.entityAccessors=true",
            "-Aquerydsl.useFields=false"
        ))
    }
}

// 清理任务
tasks.clean {
    doLast {
        delete("src/main/generated")
    }
}
