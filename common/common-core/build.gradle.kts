// Java version inherited from parent build.gradle.kts (Java 17)

dependencies {
    // common (使用自定义 BOM 管理的版本)
    api("com.google.guava:guava")
    api("commons-io:commons-io")
    api("org.apache.commons:commons-lang3")
    api("commons-codec:commons-codec")

    // DB (Spring Boot BOM 和自定义 BOM 管理的版本)
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("com.alibaba:druid")
    api("mysql:mysql-connector-java")
    api("com.h2database:h2")

    // util (使用自定义 BOM 管理的版本)
    api("com.alibaba:fastjson")
    api("org.javatuples:javatuples")

    // web (Spring Boot BOM 管理的版本)
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-aop")

    // web (使用自定义 BOM 管理的版本)
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui")

    // other (使用自定义 BOM 管理的版本)
    api("com.google.code.findbugs:annotations")
    
    // Lombok
    compileOnly("org.projectlombok:lombok")

    // JUnit for tests
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
