# CLAUDE.md

## Build Commands

```bash
./gradlew build                    # Full build (compile + spotless + test)
./gradlew :archforge-server-admin:bootRun     # Start Spring Boot (dev profile)
./gradlew spotlessApply            # Auto-fix code formatting
./gradlew spotlessCheck            # Check formatting only
./gradlew test                     # Run all tests
./gradlew :archforge-server-admin:test       # Run specific module tests
./gradlew clean build              # Clean + full build
```

- Requires **Java 25** — `JAVA_HOME` must point to JDK 25 (e.g. Azul Zulu)
- Dev mode requires **Docker** — Testcontainers auto-starts PostgreSQL, Redis, RustFS

## Architecture

Multi-module Spring Boot 4 project with DDD + Clean Architecture:

```
ArchForge/
├── common/common-base        # Base utilities, enums, encryption, Jackson, validation
├── common/common-error       # ErrorCode, exceptions, error manager
├── common/common-jpa         # JPA base entities, converters, query helpers
├── domain/admin-user/        # User/Role/Menu/Dept domain entities + services
├── server-admin/             # Controllers, Spring Boot entry point, security
├── example/example-task/     # Example bounded context
└── dependencies/             # Centralized version management (java-platform)
```

### Key Patterns

- **DDD rich domain model**: entities have business methods + state machines (e.g. `Task.complete()`, `OrderStatus.canTransitionTo()`)
- **Type-safe queries**: Hibernate Static Metamodel (`Entity_` classes) + `SafeExpr`/`AliasExpr` for compile-time field validation; `QueryHelp` + JPA Specifications for dynamic filtering
- **ScopedValue context**: `ScopedValueContext` replaces ThreadLocal for request context propagation (JDK 25)
- **Structured Concurrency**: `StructuredTaskScope` for parallel operations (e.g. ServerMonitorService)
- **Pattern Matching**: switch expressions with type patterns throughout error handling and JSON utils
- **MapStruct**: `@Mapper` interfaces for Entity→DTO conversion (under `server-admin/.../mapper/`)
- **Multi-datasource**: `dynamic-datasource-spring-boot4-starter` with master/slave + `GroupDataSourceProxy` for JPA
- **JWT auth**: `JwtTokenUtil` + `JwtAuthenticationFilter` + Spring Security `SecurityFilterChain`
- **File storage**: `FileStorageService` interface with Local and S3 implementations
- **Problem Details**: RFC 9457 `ProblemDetail` for error responses
- **Observation API**: Micrometer `Observation` in `RequestLogFilter` for metrics/tracing
- **Stream Gatherers**: `CollectionUtils.partition()` via `Gatherers.windowFixed()`

## Code Style

- **Spotless + Google Java Style** (AOSP 4-space indent) — enforced on build
- **Lombok**: use `@Data`, `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`
- **DO NOT use `var`** — always specify explicit types
- **DO NOT import `cn.hutool:hutool-all`** — use JDK, Apache Commons, Guava, or Spring
- **DO NOT use field injection** — use constructor injection via `@RequiredArgsConstructor`
- **JSpecify `@NullMarked`** on packages, `@Nullable` on nullable parameters/returns
- Follow [Alibaba Java Coding Guidelines](https://github.com/alibaba/Alibaba-Java-Coding-Guidelines)

## Configuration

- Config prefix: `arch-forge` — mapped to `ArchForgeProperties` bean
- Profiles: `dev` (default), `test`, `staging`, `prod`
  - `application.yaml`: base config
  - `application-dev.yaml`: local Docker PostgreSQL/Redis/RustFS, script-initialized SQL
  - `application-test.yaml`: Testcontainers + Flyway + seed data
  - `application-staging.yaml`: external services, Flyway
  - `application-prod.yaml`: production hardened, Flyway
- Logging: Log4j2 via `log4j2-spring.xml` with `<SpringProfile>` sections
- Database: PostgreSQL via Flyway migrations (`server-admin/src/main/resources/db/migration/`) and `domain/admin-user/src/main/resources/sql/` seed data

## JDK 25 Features

- `--enable-preview` enabled globally (build.gradle.kts + processAot + processTestAot + bootRun)
- `ScopedValue<RequestContext>` in `ScopedValueContext` (replaces ThreadLocal)
- `StructuredTaskScope.open()` + `fork()` + `join()` for parallel tasks
- `Gatherers.windowFixed()` for stream partitioning
- Pattern matching `switch` with type patterns and `null` cases
- `--enable-native-access=ALL-UNNAMED` for Netty compatibility

## Testing

- **Spock 2.4** (Groovy 5.x) for BDD tests — `src/test/groovy/`
- **JUnit 6** (Jupiter) for unit tests — `src/test/java/`
- **RestClient integration tests** — `RestClientIntegrationTest.java` (15 API tests)
- **Testcontainers** in test: PostgreSQL 17, Redis 7, RustFS (auto-started)
- Test data: Flyway migrations + `InitDbMockServer` SQL seed data

## Dependencies

All versions centrally managed in `dependencies/build.gradle.kts`:
- Spring Boot 4.0.5, Gradle 9.4.1, PostgreSQL 42.7.10
- Micrometer 1.6.4 + OpenTelemetry 1.55.0, Flyway 11.14.1
- JSpecify 1.0.0, MapStruct 1.6.3, Lombok 1.18.44
- Testcontainers 2.0.4, Spock 2.4, JUnit 6.0.3


