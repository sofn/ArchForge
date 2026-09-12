plugins {
    id("java-test-fixtures")
}

dependencies {
    // 项目内依赖
    api(project(":archforge-common:archforge-common-base"))

    // testFixtures 源集不继承 implementation，需显式引入版本平台
    "testFixturesImplementation"(platform("org.springframework.boot:spring-boot-dependencies:4.1.0"))
    "testFixturesImplementation"(platform(project(":archforge-dependencies")))
    // 集成测试容器基座（testsupport.AbstractIntegrationTest），server-admin / server-web 共用。
    // testFixturesApi 才能把类型传递给消费方；testFixtures 变体不进入生产 jar。
    "testFixturesApi"("org.testcontainers:testcontainers")
    "testFixturesApi"("org.testcontainers:testcontainers-postgresql")
    "testFixturesApi"("org.springframework:spring-test")
    "testFixturesApi"("org.jspecify:jspecify")

    // JPA / 数据库 (Spring Boot BOM 和自定义 BOM 管理的版本)
    api("org.springframework.boot:spring-boot-starter-data-jpa") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    api("com.baomidou:dynamic-datasource-spring-boot4-starter")
    api("com.alibaba:druid")
    api("org.postgresql:postgresql")

    // Flyway：schema 迁移由 common.persistence.FlywayConfig 手工装配
    //（Boot 4 无 Flyway 自动配置；migration SQL 资源也在本模块，server-admin/server-web 共用）
    api("org.flywaydb:flyway-core")
    api("org.flywaydb:flyway-database-postgresql")

    // Hibernate Static Metamodel Generator (类型安全字段引用，替代 QueryDSL Q-classes)
    // 版本与 Spring Boot 4.1.0 BOM 对齐 (Hibernate 7.4.x)
    annotationProcessor("org.hibernate.orm:hibernate-processor")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
