# 项目依赖管理说明

## 概述

本项目采用两种方式管理依赖版本，实现类似于Maven的`dependencyManagement`功能：

1. **Spring Boot BOM**: 用于管理 Spring Boot 生态相关依赖的版本
2. **自定义 BOM**: 用于管理项目中使用的第三方依赖的版本

通过这两种BOM（Bill of Materials），子模块可以使用简洁的依赖声明方式，无需指定版本号。

## BOM配置详情

### Spring Boot BOM

在根项目的 `build.gradle.kts` 中配置：

```kotlin
dependencies {
    // 引入 Spring Boot BOM 进行依赖版本管理
    add("implementation", platform("org.springframework.boot:spring-boot-dependencies:3.3.2"))
    
    // JUnit BOM
    add("testImplementation", platform("org.junit:junit-bom:5.10.0"))
}
```

### 自定义 BOM

在 `dependencies` 模块中配置，文件路径：`dependencies/build.gradle.kts`

```kotlin
plugins {
    `java-platform`
    `maven-publish`
}

dependencies {
    constraints {
        api("com.alibaba:druid:1.2.24")
        api("mysql:mysql-connector-java:8.0.33")
        api("com.h2database:h2:2.2.224")
        api("com.google.guava:guava:32.1.2-jre")
        api("commons-io:commons-io:2.16.0")
        api("org.apache.commons:commons-lang3:3.17.0")
        api("commons-codec:commons-codec:1.15")
        api("com.alibaba:fastjson:1.2.83")
        api("org.javatuples:javatuples:1.2")
        api("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
        api("com.google.code.findbugs:annotations:3.0.0")
        api("org.testcontainers:testcontainers:1.18.3")
        api("org.testcontainers:junit-jupiter:1.18.3")
        api("org.testcontainers:mysql:1.18.3")
        api("org.jolokia:jolokia-core:1.7.2")
    }
}
```

## 使用方式

在子模块中，现在可以使用以下方式声明依赖：

```kotlin
dependencies {
    // Spring Boot 依赖（由 Spring Boot BOM 管理版本）
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    // 第三方依赖（由自定义 BOM 管理版本）
    implementation("com.alibaba:druid")
    implementation("mysql:mysql-connector-java")
    implementation("com.google.guava:guava")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")
    
    // 测试依赖（由 JUnit BOM 管理版本）
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    
    // Testcontainers 相关依赖（由自定义 BOM 管理版本）
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")
}
```

## 根项目配置

在根项目的 `build.gradle.kts` 中，我们为所有子项目（除了 dependencies 模块）引入了这两个 BOM：

```kotlin
subprojects {
    if (name != "dependencies") {
        apply(plugin = "java-library")
        
        dependencies {
            // 引入 Spring Boot BOM
            add("implementation", platform("org.springframework.boot:spring-boot-dependencies:3.3.2"))
            
            // 引入自定义 BOM
            add("implementation", platform(project(":dependencies")))
        }
    }
}
```

## 添加新的依赖到 BOM

### 1. 添加 Spring Boot 相关依赖

如果新依赖是 Spring Boot 生态的一部分，可以直接添加而无需指定版本：

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
}
```

### 2. 添加其他依赖

对于非 Spring Boot 的依赖，需要在 `dependencies/build.gradle.kts` 的 `constraints` 块中添加版本和依赖定义：

```kotlin
dependencies {
    constraints {
        api("org.example:example-library:1.0.0")
    }
}
```

然后在项目中使用：

```kotlin
dependencies {
    implementation("org.example:example-library")
}
```

## 优势

1. **统一版本管理**: 所有依赖版本在 BOM 中集中管理，便于维护
2. **简化依赖声明**: 子模块无需指定版本号，减少重复代码
3. **避免版本冲突**: 通过 BOM 确保依赖版本一致性
4. **易于维护**: 版本升级只需修改 BOM 配置