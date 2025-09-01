# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

- `./gradlew build`: Build the entire project
- `./gradlew server-admin:bootRun`: Start the Spring Boot admin server
- `./gradlew clean build`: Clean build the project
- `./gradlew test`: Run all tests
- `./gradlew :server-admin:test`: Run tests for specific module

## Development

- Main application entry point: `server-admin/src/main/java/com/lesofn/appboot/server/admin/Application.java`
- Default ports: Application (8080), Management (7002)
- Development profile uses H2 database with console at `/h2-console`
- Hot reload enabled via Spring Boot devtools
- Swagger UI available at: `http://localhost:8080/swagger-ui/index.html`

## Architecture

This is a multi-module Spring Boot 3 project with clean architecture principles:

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
- Spring Boot 3.5.4 with Java 17
- Key libraries: Druid (connection pooling), Guava, Apache Commons, SpringDoc OpenAPI
- Testing: JUnit 5, Testcontainers, Spock Framework
- Database: H2 (dev), MySQL (production)

## Code Style

- Follow Alibaba Java coding guidelines: https://github.com/alibaba/Alibaba-Java-Coding-Guidelines
- Do not import `cn.hutool:hutool-all` - use standard JDK, Apache Commons, Guava, or Spring utilities instead
- Lombok annotations used throughout for reducing boilerplate

## Configuration

- Environment-specific configs in `server-admin/src/main/profiles/{env}/application.yaml`
- Profiles: `dev` (H2 + mock Redis), `test`, `prod`
- JWT secret and Redis configuration environment-specific
- Gradle profile set in `gradle.properties`

## Monitoring

- Health check: `http://localhost:7002/health`
- Metrics: `http://localhost:7002/metrics`
- Druid monitoring: `http://localhost:8080/druid`
- All actuator endpoints exposed on management port 7002