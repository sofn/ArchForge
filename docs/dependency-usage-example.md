# 依赖使用示例

本文档展示了如何在项目中使用统一管理的依赖，无需指定版本号。

## 完整示例

```kotlin
dependencies {
    // 示例：使用 Spring Boot Starter Web（版本由 Spring Boot BOM 管理）
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    // 示例：使用 Spring Boot Starter Data JPA（版本由 Spring Boot BOM 管理）
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    
    // 示例：使用 Spring Boot Starter AOP（版本由 Spring Boot BOM 管理）
    implementation("org.springframework.boot:spring-boot-starter-aop")
    
    // 示例：使用 Lombok（版本由 BOM 管理）
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    // 示例：使用自定义 BOM 管理的依赖
    implementation("com.alibaba:druid")
    implementation("com.google.guava:guava")
    implementation("org.apache.commons:commons-lang3")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")
    
    // 示例：使用 JUnit Jupiter（版本由 JUnit BOM 管理）
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    
    // 示例：使用 Testcontainers（版本由自定义 BOM 管理）
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")
}
```

## 使用说明

1. 对于Spring Boot生态中的依赖，直接引用即可，无需指定版本号
2. 对于项目中定义的第三方依赖，同样无需指定版本号
3. 对于测试相关的依赖，也由相应的BOM管理版本

这种方式简化了依赖管理，确保了版本一致性，并减少了在每个模块中重复指定版本号的工作。