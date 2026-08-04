dependencies {
    api(project(":common:common-base"))
    api(project(":common:common-jpa"))
    api(project(":infrastructure"))

    implementation("org.freemarker:freemarker")
    implementation("org.apache.commons:commons-csv:1.10.0")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.hibernate.orm:hibernate-processor:7.4.1.Final")

    compileOnly(platform(project(":dependencies")))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
