plugins {
    id("org.springframework.boot") version "3.3.2"
}

// 构建可执行jar/war包
configurations {
    create("providedRuntime")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}

dependencies {
    api(project(":common-core"))
    api(project(":frame"))
    api(project(":example:example-user"))
    api(project(":example:example-task"))

    api("org.springframework.boot:spring-boot-starter-web")
    api("org.jolokia:jolokia-core")
}

// profile环境配置文件
sourceSets {
    main {
        resources {
            setSrcDirs(listOf("src/main/resources/", "src/main/profiles/${findProperty("profile") ?: "dev"}"))
        }
    }
}
