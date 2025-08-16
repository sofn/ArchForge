group = "com.lesofn.matrixboot"
version = "0.1.RELEASE"

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
        
        dependencies {
            // 引入 Spring Boot dependencies
            add("implementation", platform("org.springframework.boot:spring-boot-dependencies:3.3.2"))
            // 引入自定义 dependencies
            add("implementation", platform(project(":dependencies")))

            // compile - Lombok配置
            val lombokVersion = "1.18.30"
            add("annotationProcessor", "org.projectlombok:lombok:$lombokVersion")
            add("testAnnotationProcessor", "org.projectlombok:lombok:$lombokVersion")

        }
    }
}
