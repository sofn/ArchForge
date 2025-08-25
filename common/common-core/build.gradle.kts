apply(plugin = "groovy")

dependencies {
    // 项目内依赖
    api(project(":common:common-error"))

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
    api("org.javatuples:javatuples")

    // HTTP客户端
    api("com.konghq:unirest-java-core")

    // Jackson for JSON processing (Spring Boot BOM 管理的版本)
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.core:jackson-annotations")

    // web (Spring Boot BOM 管理的版本)
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-aop")

    // web (使用自定义 BOM 管理的版本)
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui")

    // other (使用自定义 BOM 管理的版本)
    api("com.google.code.findbugs:annotations")
    api("org.lionsoul:ip2region")
    api("eu.bitwalker:UserAgentUtils")
    
    // Lombok
    compileOnly("org.projectlombok:lombok")

    // JUnit for tests
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    
    // Spock for tests
    testImplementation("org.spockframework:spock-core")
    testImplementation("org.spockframework:spock-spring")
    
    // Groovy for Spock
    testImplementation("org.codehaus.groovy:groovy")
}
