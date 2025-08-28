plugins {
    `java-platform`
    `maven-publish`
}

group = "com.lesofn.matrixboot"
version = "0.1.RELEASE"

// 配置平台，允许定义依赖约束
javaPlatform {
    allowDependencies()
}

dependencies {
    // 定义依赖约束，这些依赖不会被直接引入，但会为使用它们的项目提供版本管理
    constraints {
        // 数据库相关
        api("com.alibaba:druid:1.2.24")
        api("mysql:mysql-connector-java:8.0.33")
        api("com.h2database:h2:2.3.232")
        
        // 常用工具类
        api("com.google.guava:guava:33.3.1-jre")
        api("commons-io:commons-io:2.17.0")
        api("org.apache.commons:commons-lang3:3.17.0")
        api("commons-codec:commons-codec:1.17.1")
        
        // 实用工具
        api("org.javatuples:javatuples:1.2")
        
        // HTTP客户端
        api("com.konghq:unirest-java-core:4.5.0")
        
        // Web相关
        api("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
        api("org.jolokia:jolokia-core:1.7.2")
        
        // JWT
        api("io.jsonwebtoken:jjwt-api:0.11.5")
        api("io.jsonwebtoken:jjwt-impl:0.11.5")
        api("io.jsonwebtoken:jjwt-jackson:0.11.5")
        
        // QueryDSL
        api("com.querydsl:querydsl-jpa:5.1.0")
        api("com.querydsl:querydsl-apt:5.1.0")
        api("jakarta.persistence:jakarta.persistence-api:3.1.0")
        
        // 其他
        api("com.google.code.findbugs:annotations:3.0.1")
        api("org.lionsoul:ip2region:2.7.0")
        api("eu.bitwalker:UserAgentUtils:1.21")
        
        // Lombok and SLF4J
        api("org.projectlombok:lombok:1.18.36")
        api("org.slf4j:slf4j-api:2.0.16")
        api("org.slf4j:slf4j-simple:2.0.16")
        
        // 测试相关
        api("org.junit.jupiter:junit-jupiter-api:5.11.3")
        api("org.junit.jupiter:junit-jupiter-engine:5.11.3")
        api("org.testcontainers:testcontainers:1.20.4")
        api("org.testcontainers:junit-jupiter:1.20.4")
        api("org.testcontainers:mysql:1.20.4")
        api("org.spockframework:spock-core:2.3-groovy-3.0")
        api("org.spockframework:spock-spring:2.3-groovy-3.0")
        api("org.codehaus.groovy:groovy:3.0.17")

        // Redis Mock for Dev environment
        api("com.github.fppt:jedis-mock:1.1.11")
        
        // Kaptcha 验证码
        api("com.github.penggle:kaptcha:2.3.2")

    }
}

publishing {
    publications {
        create<MavenPublication>("bom") {
            from(components["javaPlatform"])
            artifactId = "matrixboot-dependencies"
        }
    }
}
