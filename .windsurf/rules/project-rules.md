# AppBoot Project Rules for Windsurf

This file provides guidance when working with code in the AppBoot repository.

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
- `infrastructure/`: Cross-cutting infrastructure concerns (auth, logging, configuration, etc.)
- `server-admin`: Web layer and main application entry point
- `dependencies/`: Centralized dependency version management
- `example/`: Example implementations

### Key Patterns
- Uses domain-driven design with separate modules for different bounded contexts
- Authentication handled via JWT tokens with configurable expiration
- Multi-datasource support with master/slave configuration
- Profile-based configuration (dev/test/prod) with separate YAML files in `src/main/profiles/`
- All configurations should be centralized in `AppBootConfig` bean

## Dependencies

- All dependency versions centrally managed in `dependencies/build.gradle.kts`
- Spring Boot 3.5.4 with Java 17
- Key libraries: Druid (connection pooling), Guava, Apache Commons, SpringDoc OpenAPI
- Testing: JUnit 5, Testcontainers, Spock Framework
- Database: H2 (dev), MySQL (production)

## Code Style Guidelines

### General Rules
- Follow Alibaba Java coding guidelines: https://github.com/alibaba/Alibaba-Java-Coding-Guidelines
- **NEVER** import `cn.hutool:hutool-all` - use standard JDK, Apache Commons, Guava, or Spring utilities instead
- Always use explicit types - **DO NOT** use `var` keyword

### Lombok Usage
- Use Lombok annotations actively for reducing boilerplate code:
  - `@Data` for POJOs with getters, setters, equals, hashCode, and toString
  - `@Getter` and `@Setter` for field-level or class-level accessor generation
  - `@Builder` for builder pattern implementation
  - `@RequiredArgsConstructor` for constructor injection in Spring components
  - `@NoArgsConstructor` and `@AllArgsConstructor` when needed
  - `@Slf4j` for logging
- **DO NOT** use Lombok's `var` or `val` - always specify explicit types

### Spring Best Practices
- Use constructor injection over field injection
- Prefer `@RequiredArgsConstructor` with `final` fields for dependency injection
- Use `@ConfigurationProperties` for configuration classes

## Configuration Management

### Configuration Structure
- Common configurations in `server-admin/src/main/resources/application-basic.yaml`
- Environment-specific configs in `server-admin/src/main/profiles/{env}/application.yaml`
- Profiles: `dev` (H2 + mock Redis), `test`, `prod`

### Configuration Bean Usage
- All application configurations should be managed through `AppBootConfig` bean
- Avoid using `@Value` annotations directly - use `AppBootConfig` instead
- Configuration structure:
  ```java
  @ConfigurationProperties(prefix = "app-boot")
  public class AppBootConfig {
      private Token token;
      private Jwt jwt;
      private Captcha captcha;
      // ... other configurations
  }
  ```

### Environment-specific Settings
- JWT secret and expiration time vary by environment
- Redis configuration environment-specific
- Database connections use H2 for dev, MySQL for test/prod
- Captcha disabled in dev, enabled in test/prod

## Monitoring

- Health check: `http://localhost:7002/health`
- Metrics: `http://localhost:7002/metrics`
- Druid monitoring: `http://localhost:8080/druid`
- All actuator endpoints exposed on management port 7002

## Security Considerations

- JWT tokens for authentication with configurable expiration
- Captcha validation configurable per environment
- Password encryption using BCrypt
- RBAC (Role-Based Access Control) with users, roles, and menus

## Testing Strategy

- Unit tests with JUnit 5
- Integration tests with Testcontainers
- Spock Framework for BDD-style testing
- Mock Redis for development environment testing

## Important Notes

1. When modifying configuration properties, always update `AppBootConfig` bean
2. Maintain clean separation between modules - don't create circular dependencies
3. Use appropriate Lombok annotations but avoid `var` keyword
4. Follow DDD principles for domain logic organization
5. Keep environment-specific configurations minimal and well-documented