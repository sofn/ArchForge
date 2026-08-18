dependencies {
    api(project(":archforge-common:archforge-common-jpa"))
    api(project(":archforge-infrastructure"))

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.hibernate.orm:hibernate-processor:7.4.1.Final")

    compileOnly(platform(project(":archforge-dependencies")))
    compileOnly("org.mapstruct:mapstruct")
    annotationProcessor(platform(project(":archforge-dependencies")))
    annotationProcessor("org.mapstruct:mapstruct-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
