# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

- `./gradlew build`: Build the entire project
- `./gradlew server-admin:bootRun`: Start the Spring Boot admin server
- `./gradlew clean build`: Clean build the project
- `./gradlew test`: Run all tests
- `./gradlew :server-admin:test`: Run tests for specific module
- Requires Java 21 (`JAVA_HOME` must point to a JDK 21 installation)

## Development

- Main application entry point: `server-admin/src/main/java/com/lesofn/appforge/server/admin/Application.java`
- Default ports: Application (8080), Management (7002)
- Development profile uses H2 database with console at `/h2-console`
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
- `infrastructure/`: Cross-cutting infrastructure concerns (auth, logging, etc.)
- `server-admin`: Web layer and main application entry point
- `dependencies/`: Centralized dependency version management
- `example/`: Example implementations

### Key Patterns
- Uses domain-driven design with separate modules for different bounded contexts
- Authentication handled via JWT tokens with configurable expiration
- Multi-datasource support with master/slave configuration
- Profile-based configuration (dev/test/prod) with separate YAML files in `src/main/profiles/`

## Dependencies

- All dependency versions centrally managed in `dependencies/build.gradle.kts`
- Spring Boot 4.0.5 with Java 21
- Gradle 9.4.1 with configuration cache support
- Key libraries: Druid (connection pooling), Guava, Apache Commons, SpringDoc OpenAPI
- Testing: JUnit 6, Testcontainers, Spock 2.3 (Groovy 4.x)
- Database: H2 (dev), MySQL (production)
- Groovy is forced to 4.0.31 (overriding SB4's Groovy 5.x) for Spock 2.3 compatibility

## Code Style

- Follow Alibaba Java coding guidelines: https://github.com/alibaba/Alibaba-Java-Coding-Guidelines
- Do not import `cn.hutool:hutool-all` - use standard JDK, Apache Commons, Guava, or Spring utilities instead
- Lombok annotations used throughout for reducing boilerplate
- Use Lombok annotations actively (@Data, @Getter, @Setter, @Builder, @RequiredArgsConstructor, etc.) but DO NOT use `var` keyword - always specify explicit types

## Configuration

- Environment-specific configs in `server-admin/src/main/profiles/{env}/application.yaml`
- Profiles: `dev` (H2 + mock Redis), `test`, `prod`
- JWT secret and Redis configuration environment-specific
- Gradle profile set in `gradle.properties`
- Config prefix: `app-forge`

## Monitoring

- Health check: `http://localhost:7002/health`
- Metrics: `http://localhost:7002/metrics`
- Druid monitoring: `http://localhost:8080/druid`
- All actuator endpoints exposed on management port 7002
