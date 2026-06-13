
dependencies {
    // 项目内依赖
    api(project(":common:common-error"))

    // common (使用自定义 BOM 管理的版本)
    api("com.google.guava:guava")
    api("commons-io:commons-io")
    api("org.apache.commons:commons-lang3")
    api("commons-codec:commons-codec")
    api("org.apache.commons:commons-collections4")

    // DB (Spring Boot BOM 和自定义 BOM 管理的版本)
    api("org.springframework.boot:spring-boot-starter-data-jpa") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    api("com.baomidou:dynamic-datasource-spring-boot4-starter")
    api("org.postgresql:postgresql")
    
    // Hibernate Static Metamodel Generator (类型安全字段引用，替代 QueryDSL Q-classes)
    // 版本与 Spring Boot 4.0.7 BOM 对齐 (Hibernate 7.2.x)
    annotationProcessor("org.hibernate.orm:hibernate-processor:7.2.19.Final")

    // util (使用自定义 BOM 管理的版本)
    api("org.javatuples:javatuples")

    // HTTP客户端
    api("com.konghq:unirest-java-core")

    // Jackson for JSON processing (Spring Boot BOM 管理的版本)
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.core:jackson-annotations")

    // web (Spring Boot BOM 管理的版本)
    api("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    // AspectJ support (replaces spring-boot-starter-aop in Spring Boot 4)
    api("org.springframework.boot:spring-boot-starter-aspectj") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }

    // web (使用自定义 BOM 管理的版本)
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui")

    // other (使用自定义 BOM 管理的版本)
    api("com.google.code.findbugs:annotations")
    api("org.lionsoul:ip2region")
    api("eu.bitwalker:UserAgentUtils")
    api("org.jspecify:jspecify")

    // Excel I/O - FastExcel (高性能、无 POI 依赖；项目标准 Excel 库)
    api("org.dhatim:fastexcel")
    api("org.dhatim:fastexcel-reader")
    
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

}
