dependencies {
    api(project(":infrastructure"))

    // Spring Data JPA
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    // Spring Security
    api("org.springframework.boot:spring-boot-starter-security")

    // QueryDSL
    api("com.querydsl:querydsl-jpa")
    api("com.querydsl:querydsl-apt")
    api("jakarta.persistence:jakarta.persistence-api")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    annotationProcessor("com.querydsl:querydsl-apt:5.0.0")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.1.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
