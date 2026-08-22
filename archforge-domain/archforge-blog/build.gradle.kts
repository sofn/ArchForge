plugins {
    id("java-test-fixtures")
}

dependencies {
    // testFixtures 源集不继承 implementation, 需显式引入版本平台
    "testFixturesImplementation"(platform("org.springframework.boot:spring-boot-dependencies:4.1.0"))
    "testFixturesImplementation"(platform(project(":archforge-dependencies")))

    api(project(":archforge-common:archforge-common-jpa"))

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.hibernate.orm:hibernate-processor:7.4.1.Final")

    compileOnly(platform(project(":archforge-dependencies")))
    compileOnly("org.mapstruct:mapstruct")
    annotationProcessor(platform(project(":archforge-dependencies")))
    annotationProcessor("org.mapstruct:mapstruct-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
