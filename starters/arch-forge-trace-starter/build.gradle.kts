dependencies {
    api("org.springframework.boot:spring-boot-starter-actuator") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }
    api("io.micrometer:micrometer-tracing")
    api("io.micrometer:micrometer-tracing-bridge-otel")
    api("io.micrometer:micrometer-registry-prometheus")
    api("io.opentelemetry:opentelemetry-exporter-otlp")

    compileOnly("org.projectlombok:lombok")
}
