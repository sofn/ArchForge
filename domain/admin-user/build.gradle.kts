dependencies {
    api(project(":infrastructure"))

    // Spring Data JPA
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    // Spring Security
    api("org.springframework.boot:spring-boot-starter-security")

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

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

sourceSets {
    main {
        java {
            srcDirs("src/main/java", "src/main/generated")
        }
    }
}

// 配置注解处理器选项
tasks.withType<JavaCompile> {
    options.apply {
        compilerArgs.addAll(listOf(
            "-Aquerydsl.entityAccessors=true",
            "-Aquerydsl.useFields=false"
        ))
        // 禁用增量编译以避免Q类重复生成问题
        isIncremental = false
    }
}
