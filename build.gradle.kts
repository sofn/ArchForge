group = "com.lesofn.appboot"
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
            add("implementation", platform("org.springframework.boot:spring-boot-dependencies:3.5.4"))
            // 引入自定义 dependencies
            add("implementation", platform(project(":dependencies")))

            // compile - Lombok配置 (版本由 dependencies 模块管理)
            add("annotationProcessor", "org.projectlombok:lombok:1.18.36")
            add("testAnnotationProcessor", "org.projectlombok:lombok:1.18.36")

            // 全局测试依赖 - Spock框架 (版本由 dependencies 模块管理)
            add("testImplementation", "org.junit.jupiter:junit-jupiter-api")
            add("testRuntimeOnly", "org.junit.jupiter:junit-jupiter-engine")
            add("testImplementation", "org.spockframework:spock-core")
            add("testImplementation", "org.spockframework:spock-spring")
            add("testImplementation", "org.springframework.boot:spring-boot-starter-test") {
                exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
                exclude(group = "ch.qos.logback", module = "logback-classic")
                exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
            }
            add("testImplementation", "org.codehaus.groovy:groovy")
            add("testImplementation", "org.junit.platform:junit-platform-launcher")
        }
    }
}
