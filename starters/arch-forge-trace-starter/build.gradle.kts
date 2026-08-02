dependencies {
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("io.micrometer:micrometer-tracing")
    api("io.micrometer:micrometer-tracing-bridge-otel")
    api("io.micrometer:micrometer-registry-prometheus")
    api("io.opentelemetry:opentelemetry-exporter-otlp")
}
