plugins {
    id("com.diffplug.spotless") version "8.4.0" apply false
}

group = "com.lesofn.archsmith"
version = "0.1.SNAPSHOT"

allprojects {
    repositories {
        // 阿里云镜像（首选）
        maven { url = uri("https://maven.aliyun.com/repository/public/") }
        maven { url = uri("https://maven.aliyun.com/repository/spring/") } // Spring 生态专用
        maven { url = uri("https://maven.aliyun.com/repository/google/") } // Google 依赖专用

        // 腾讯云镜像（备选）
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
 
        // 华为云镜像（备选）
        maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
 
        // 原始仓库（如果镜像源找不到依赖，回退到中央仓库）
        mavenCentral()
        google()
    }
}

subprojects {
    // 为除了 dependencies 之外的所有子项目应用插件
    if (name != "dependencies") {
        apply(plugin = "java-library")
        apply(plugin = "groovy")
        apply(plugin = "com.diffplug.spotless")

        // Spotless 代码格式化 - Google Java Style (AOSP: 4-space indent)
        configure<com.diffplug.gradle.spotless.SpotlessExtension> {
            lineEndings = com.diffplug.spotless.LineEnding.UNIX
            java {
                target("src/*/java/**/*.java")
                googleJavaFormat("1.35.0").aosp()
                removeUnusedImports()
                trimTrailingWhitespace()
                endWithNewline()
            }
        }

        // 配置 Java 25
        configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(25))
            }
        }

        tasks.withType<JavaCompile> {
            options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-parameters"))
        }
        
        // 配置测试任务使用JUnit Platform
        tasks.withType<Test> {
            useJUnitPlatform()
        }

        // 全局排除冲突的日志依赖
        configurations.all {
            exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
            exclude(group = "ch.qos.logback", module = "logback-classic")
            exclude(group = "ch.qos.logback", module = "logback-core")
            exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
        }
        
        dependencies {
            // 引入 Spring Boot dependencies
            add("implementation", platform("org.springframework.boot:spring-boot-dependencies:4.0.5"))
            // 引入自定义 dependencies
            add("implementation", platform(project(":dependencies")))

            // compile - Lombok配置
            add("annotationProcessor", "org.projectlombok:lombok:1.18.44")
            add("testAnnotationProcessor", "org.projectlombok:lombok:1.18.44")

            // 全局测试依赖 - Spock 2.4 (Groovy 5.x)
            add("testImplementation", "org.junit.jupiter:junit-jupiter-api")
            add("testRuntimeOnly", "org.junit.jupiter:junit-jupiter-engine")
            add("testImplementation", "org.spockframework:spock-core")
            add("testImplementation", "org.spockframework:spock-spring")
            add("testImplementation", "org.springframework.boot:spring-boot-starter-test") {
                exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
                exclude(group = "ch.qos.logback", module = "logback-classic")
                exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
            }
            add("testImplementation", "org.apache.groovy:groovy")
            add("testImplementation", "org.junit.platform:junit-platform-launcher")
        }
    }
}
