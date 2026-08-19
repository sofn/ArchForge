dependencies {
    api(project(":archforge-common:archforge-common-base"))
    api(project(":archforge-common:archforge-common-jpa"))

    implementation("org.freemarker:freemarker")
    implementation("org.apache.commons:commons-csv:1.10.0")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.hibernate.orm:hibernate-processor:7.4.1.Final")

    compileOnly(platform(project(":archforge-dependencies")))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
