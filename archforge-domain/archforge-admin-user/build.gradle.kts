plugins {
    id("java-test-fixtures")
}

dependencies {
    // testFixtures 源集不继承 implementation, 需显式引入版本平台
    "testFixturesImplementation"(platform("org.springframework.boot:spring-boot-dependencies:4.1.0"))
    "testFixturesImplementation"(platform(project(":archforge-dependencies")))

    api(project(":archforge-common:archforge-common-jpa"))
    // 不再依赖 infrastructure：auth/dictionary SPI 类型已下沉 common-base
    //（common.auth / common.dictionary），消除 domain→infra 倒挂

    // 密码编码 port 适配器（user.infrastructure.adapter.port.BCryptPasswordEncoderPort）
    //——原先由 infrastructure 的 api() 传递，现在直接声明
    implementation("org.springframework.security:spring-security-crypto")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.hibernate.orm:hibernate-processor")

    compileOnly(platform(project(":archforge-dependencies")))
    compileOnly("org.mapstruct:mapstruct")
    annotationProcessor(platform(project(":archforge-dependencies")))
    annotationProcessor("org.mapstruct:mapstruct-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
