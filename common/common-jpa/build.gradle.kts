dependencies {
    // 项目内依赖
    api(project(":common:common-base"))

    // JPA / 数据库 (Spring Boot BOM 和自定义 BOM 管理的版本)
    api("org.springframework.boot:spring-boot-starter-data-jpa") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    api("com.baomidou:dynamic-datasource-spring-boot4-starter")
    api("org.postgresql:postgresql")

    // Hibernate Static Metamodel Generator (类型安全字段引用，替代 QueryDSL Q-classes)
    // 版本与 Spring Boot 4.0.7 BOM 对齐 (Hibernate 7.2.x)
    annotationProcessor("org.hibernate.orm:hibernate-processor:7.2.19.Final")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
