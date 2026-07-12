dependencies {
    api(project(":common:common-jpa"))
    api(project(":infrastructure"))

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.hibernate.orm:hibernate-processor:7.2.19.Final")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
