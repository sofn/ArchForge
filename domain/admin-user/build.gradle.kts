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

// 配置生成的源文件目录
val generatedSourcesDir = layout.buildDirectory.dir("generated/sources/annotationProcessor/java/main")

sourceSets {
    main {
        java {
            srcDir("src/main/java")
            srcDir(generatedSourcesDir)
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
        // 设置生成的源文件输出目录
        generatedSourceOutputDirectory.set(generatedSourcesDir.get().asFile)
    }
}

// 清理任务
tasks.clean {
    doLast {
        delete("src/main/generated")
    }
}
