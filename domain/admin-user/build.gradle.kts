dependencies {
    api(project(":infrastructure"))

    // Spring Data JPA
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    // Spring Security
    api("org.springframework.boot:spring-boot-starter-security")

    // Hibernate Static Metamodel Generator (类型安全字段引用，替代 QueryDSL Q-classes)
    // 版本与 Spring Boot 4.0.7 BOM 对齐 (Hibernate 7.2.x)
    annotationProcessor("org.hibernate.orm:hibernate-processor:7.2.19.Final")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok:1.18.44")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
