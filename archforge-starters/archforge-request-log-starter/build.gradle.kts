dependencies {
    // 请求/响应载荷日志 starter：自包含，可独立用于任意 Boot 4 servlet 应用。
    // servlet-api compileOnly —— 由消费方 web 应用提供；@ConditionalOnWebApplication 兜底非 web 场景。
    api("org.springframework.boot:spring-boot")
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework:spring-web")
    api("org.apache.commons:commons-lang3")
    api("commons-io:commons-io")
    api("org.slf4j:slf4j-api")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    compileOnly("org.projectlombok:lombok")

    testImplementation("jakarta.servlet:jakarta.servlet-api")
}
