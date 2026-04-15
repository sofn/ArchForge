# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

- `./gradlew build`: Build the entire project
- `./gradlew server-admin:bootRun`: Start the Spring Boot admin server
- `./gradlew clean build`: Clean build the project
- `./gradlew test`: Run all tests
- `./gradlew :server-admin:test`: Run tests for specific module
- Requires Java 25 (`JAVA_HOME` must point to a JDK 25 installation, e.g. `/home/sofn/jdks/zulu25`)

## Development

- Main application entry point: `server-admin/src/main/java/com/lesofn/appforge/server/admin/Application.java`
- Default ports: Application (8080), Management (7002)
- Development profile uses Testcontainers PostgreSQL (auto-started via `InitPostgreSQLServer`)
- Testcontainers MinIO for S3-compatible file storage in dev
- Hot reload enabled via Spring Boot devtools
- Swagger UI available at: `http://localhost:8080/swagger-ui/index.html`

## Architecture

This is a multi-module Spring Boot 4 project with clean architecture principles:

### Module Structure
- `common/`: Shared utilities and error handling
  - `common-core`: Core utilities and shared components
  - `common-error`: Centralized error handling and response formats
- `domain/`: Domain-specific business logic modules
  - `admin-user`: User management domain logic
- `infrastructure/`: Cross-cutting infrastructure concerns (auth, logging, file storage, etc.)
- `server-admin`: Web layer and main application entry point
- `dependencies/`: Centralized dependency version management
- `example/`: Example implementations

### Key Patterns
- Uses domain-driven design with separate modules for different bounded contexts
- Authentication handled via JWT tokens with configurable expiration
- Multi-datasource support with master/slave configuration via `dynamic-datasource-spring-boot4-starter`
  - Datasource groups configured in YAML under `spring.datasource.dynamic.datasource`
  - `GroupDataSourceProxy` bridges dynamic-datasource groups with JPA EntityManagerFactories
  - `@DS("group_name")` annotation available for explicit datasource routing
- Profile-based configuration (dev/test/prod) using standard Spring Boot `application-{profile}.yaml`
- File storage abstraction: `FileStorageService` interface with `LocalFileStorageService` and `S3FileStorageService` implementations, configurable via `app-forge.file-storage.type` (local/s3)

## Dependencies

- All dependency versions centrally managed in `dependencies/build.gradle.kts`
- Spring Boot 4.0.5 with Java 25
- Gradle 9.4.1 with configuration cache support
- Key libraries: dynamic-datasource-spring-boot4-starter (multi-datasource + read/write split), Guava, Apache Commons, SpringDoc OpenAPI, AWS S3 SDK
- Testing: JUnit 6, Testcontainers, Spock 2.4 (Groovy 5.x), RestClient integration tests
- Database: PostgreSQL (all environments via Testcontainers in dev, real instance in prod)

## Code Style

- Follow Alibaba Java coding guidelines: https://github.com/alibaba/Alibaba-Java-Coding-Guidelines
- **Spotless + Google Java Style** enforced via `com.diffplug.spotless` plugin
  - `./gradlew spotlessCheck`: Check code formatting
  - `./gradlew spotlessApply`: Auto-fix formatting violations
- Do not import `cn.hutool:hutool-all` - use standard JDK, Apache Commons, Guava, or Spring utilities instead
- Lombok annotations used throughout for reducing boilerplate
- Use Lombok annotations actively (@Data, @Getter, @Setter, @Builder, @RequiredArgsConstructor, etc.) but DO NOT use `var` keyword - always specify explicit types

## Configuration

- Standard Spring Boot profile-based config in `server-admin/src/main/resources/`:
  - `application.yaml`: Shared base config (default profile: `dev`)
  - `application-dev.yaml`: Dev profile (Testcontainers PostgreSQL + Redis + MinIO, DevTools)
  - `application-test.yaml.example` / `application-prod.yaml.example`: Templates for test/prod
  - Real `application-test.yaml` and `application-prod.yaml` are gitignored
- Set active profile via `SPRING_PROFILES_ACTIVE` env var or JVM arg `-Dspring.profiles.active=...`
- Logging: Single `log4j2-spring.xml` with `<SpringProfile>` — Console for dev, file-only for non-dev
- Config prefix: `app-forge`

## Docker

- `docker/jvm/`: JVM mode with Project Leyden CDS/AOT optimization (Azul Zulu 25)
- `docker/native/`: Native Image mode with BellSoft Liberica NIK 25
- `docker/docker-compose.yml`: JVM mode full stack (PostgreSQL + Redis + App + Nginx)
- `docker/docker-compose.native.yml`: Native Image mode full stack
- `docker/start.sh`: One-click startup script (`./start.sh jvm` or `./start.sh native`)

## Monitoring

- Health check: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Prometheus: `http://localhost:8080/actuator/prometheus`
- **Micrometer + OpenTelemetry** integrated for distributed tracing and metrics
  - OTLP tracing/metrics export configured per profile
  - Dev: 100% sampling, Prod: 10% sampling
  - OTLP endpoint configurable via `OTEL_EXPORTER_OTLP_ENDPOINT` env var (prod)
