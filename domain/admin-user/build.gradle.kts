dependencies {
    api(project(":common:common-jpa"))
    api(project(":infrastructure"))

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.hibernate.orm:hibernate-processor:7.4.1.Final")

    compileOnly(platform(project(":dependencies")))
    compileOnly("org.mapstruct:mapstruct")
    annotationProcessor(platform(project(":dependencies")))
    annotationProcessor("org.mapstruct:mapstruct-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
