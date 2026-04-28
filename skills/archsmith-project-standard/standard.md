# ArchSmith Project Standard

> Canonical reference for technology choices, module layout, code conventions, configuration, testing, and deployment across all ArchSmith services.

---

## 1. Tech Stack

| Category | Technology | Version | Notes |
|----------|-----------|---------|-------|
| **Language** | Java (Azul Zulu) | 25 | Preview features enabled (`--enable-preview`) |
| **Framework** | Spring Boot | 4.x (currently 4.0.5) | Virtual threads enabled by default |
| **Build** | Gradle (Kotlin DSL) | 9.x (currently 9.4.1) | Configuration cache supported |
| **Database** | PostgreSQL | 17 | All environments; Testcontainers in dev |
| **Cache** | Redis | 7 | Lettuce client via Spring Data Redis |
| **Migrations** | Flyway | 11.x | `flyway-database-postgresql` dialect |
| **ORM** | Spring Data JPA + Hibernate | (Boot-managed) | `CamelCaseToUnderscores` naming strategy |
| **Multi-Datasource** | dynamic-datasource-spring-boot4-starter | 4.5.x | Master/slave groups, `@DS` annotation |
| **DTO Mapping** | MapStruct | 1.6.x | Compile-time code generation |
| **Null Safety** | JSpecify | 1.0.x | `@NullMarked` on every package |
| **API Docs** | SpringDoc OpenAPI | 2.8.x | Swagger UI at `/swagger-ui/index.html` |
| **Auth** | JWT (jjwt) | 0.12.x | Configurable expiration, auto-refresh |
| **Observability** | Micrometer + OpenTelemetry | (Boot-aligned) | OTLP export; 100% sampling dev, 10% prod |
| **Logging** | Log4j2 | (Boot-managed) | `log4j2-spring.xml` with `<SpringProfile>` |
| **Object Storage** | AWS S3 SDK | 2.31.x | `FileStorageService` abstraction (local/s3) |
| **Code Style** | Spotless + Google Java Style (AOSP) | Plugin 8.4.x, GJF 1.35.x | 4-space indent, auto-enforced |
| **Testing** | JUnit 6 + Spock 2.4 (Groovy 5.x) | See BOM | Testcontainers for integration |
| **Containerization** | Docker (multi-stage) | — | jlink minimal JRE + Project Leyden CDS/AOT |

### Prohibited Dependencies

- **`cn.hutool:hutool-all`** — Use JDK standard library, Apache Commons, Guava, or Spring utilities instead.
- **Logback** — Excluded globally; use Log4j2.
- **`var` keyword** — Always use explicit types for readability and team consistency.
- **`com.alibaba:easyexcel*`** — Forbidden by Gradle build guard (root `build.gradle.kts`). The project standard Excel I/O library is `org.dhatim:fastexcel` + `fastexcel-reader`. See §3.7 Excel I/O.
- **`org.apache.poi:*` (direct use)** — Pulled in only as a FastExcel transitive when strictly needed. Application code should use `FastExcelUtil` (in `common-core`) rather than POI APIs.

---

## 2. Module Structure

```
ArchSmith/
├── dependencies/                  # Centralized java-platform BOM
│   └── build.gradle.kts           # All third-party version constraints
├── common/
│   ├── common-core/               # Shared utilities, base entities, constants
│   │   └── build.gradle.kts
│   └── common-error/              # Error codes, exception hierarchy, response formats
│       └── build.gradle.kts
├── infrastructure/                # Cross-cutting concerns
│   └── build.gradle.kts           # Auth, config, filters, file storage, logging, observability
├── domain/
│   └── <bounded-context>/         # e.g. admin-user, order, product
│       └── build.gradle.kts       # DDD entities, repositories, domain services
├── server-<name>/                 # e.g. server-admin
│   ├── build.gradle.kts           # Spring Boot application plugin
│   └── src/main/
│       ├── java/.../Application.java
│       └── resources/
│           ├── application.yaml
│           ├── application-dev.yaml
│           └── log4j2-spring.xml
├── example/                       # Example/demo modules (not deployed)
│   └── example-task/
├── docker/
│   ├── jvm/Dockerfile             # JVM mode (jlink + Leyden CDS)
│   ├── native/Dockerfile          # GraalVM Native Image mode
│   ├── docker-compose.yml
│   ├── docker-compose.native.yml
│   ├── nginx/
│   └── start.sh                   # One-click: ./start.sh jvm | native
├── build.gradle.kts               # Root: repositories, Spotless, Java toolchain
├── settings.gradle.kts            # Module includes
└── skills/                        # Reusable Devin skills
```

### Module Dependency Rules

```
server-<name> → domain/* → common/common-core
server-<name> → infrastructure → common/common-core
                                 common/common-error
```

- **`common/common-core`**: No Spring dependencies. Pure Java utilities, base classes, constants.
- **`common/common-error`**: Error code enums, exception base classes, standard API response wrappers.
- **`infrastructure/`**: Spring-aware cross-cutting concerns — authentication, authorization, filters, interceptors, file storage, database configuration, observability setup.
- **`domain/<context>/`**: Business logic with DDD patterns. Depends on `common-core` and JPA. Contains entities with behavior, repository interfaces, domain services.
- **`server-<name>/`**: Thin web layer — controllers, request/response DTOs, Spring Boot `@SpringBootApplication` entry point. Depends on `domain/*` and `infrastructure`.
- **`dependencies/`**: `java-platform` module. All third-party versions defined here. Every other module applies `platform(project(":dependencies"))`.

### Adding a New Bounded Context

1. Create `domain/<context-name>/build.gradle.kts`
2. Add `include("domain:<context-name>")` to `settings.gradle.kts`
3. Add dependency in the server module: `implementation(project(":domain:<context-name>"))`
4. Create Flyway migrations under the server module if the context introduces new tables

---

## 3. Code Standards

### 3.1 General Rules

- Follow [Alibaba Java Coding Guidelines](https://github.com/alibaba/Alibaba-Java-Coding-Guidelines)
- **Explicit types always** — never use `var`
- Spotless enforces Google Java Style (AOSP, 4-space indent) on every build
- Run `./gradlew spotlessApply` before committing

### 3.2 Lombok Usage

Use Lombok actively to reduce boilerplate:

| Annotation | Usage |
|-----------|-------|
| `@Data` | Value objects, DTOs |
| `@Getter` / `@Setter` | When `@Data` is too broad |
| `@Builder` | Complex object construction |
| `@RequiredArgsConstructor` | Constructor injection (preferred over `@Autowired`) |
| `@Slf4j` | Logging in any class |
| `@ToString(exclude = ...)` | Avoid logging sensitive fields |
| `@EqualsAndHashCode(callSuper = true)` | Entity inheritance |

### 3.3 Dependency Injection

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // No @Autowired — constructor injection via Lombok
}
```

### 3.4 Null Safety with JSpecify

Every package must have a `package-info.java`:

```java
@NullMarked
package com.lesofn.archsmith.domain.user;

import org.jspecify.annotations.NullMarked;
```

Use `@Nullable` explicitly when null is a valid value:

```java
import org.jspecify.annotations.Nullable;

public @Nullable User findByEmail(String email) { ... }
```

### 3.5 JDK 25 Feature Adoption

#### Pattern Matching Switch

```java
// Preferred
return switch (event) {
    case UserCreated uc -> handleCreated(uc);
    case UserDeleted ud -> handleDeleted(ud);
    case null -> throw new IllegalArgumentException("event is null");
    default -> throw new UnsupportedOperationException("Unknown event: " + event);
};
```

#### Records for DTOs

```java
public record CreateUserRequest(
    String username,
    String email,
    @Nullable String phone
) {}
```

#### ScopedValue for Request Context

```java
public static final ScopedValue<RequestContext> CURRENT_REQUEST = ScopedValue.newInstance();

ScopedValue.where(CURRENT_REQUEST, context).run(() -> {
    // context available via CURRENT_REQUEST.get()
});
```

#### StructuredTaskScope for Parallel Operations

```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    Subtask<User> userTask = scope.fork(() -> userService.findById(userId));
    Subtask<List<Order>> ordersTask = scope.fork(() -> orderService.findByUser(userId));
    scope.join().throwIfFailed();
    return new UserProfile(userTask.get(), ordersTask.get());
}
```

#### Virtual Threads

Virtual threads are enabled globally via configuration:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

No code changes needed — Spring Boot 4 routes all request handling to virtual threads automatically.

### 3.6 Entity Patterns

Domain entities should contain behavior, not just data:

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    private String username;
    private String email;
    private UserStatus status;

    public static User create(String username, String email) {
        User user = new User();
        user.username = username;
        user.email = email;
        user.status = UserStatus.ACTIVE;
        return user;
    }

    public void deactivate() {
        if (this.status == UserStatus.INACTIVE) {
            throw new BusinessException(ErrorCode.USER_ALREADY_INACTIVE);
        }
        this.status = UserStatus.INACTIVE;
    }
}
```

### 3.7 Excel I/O

- **Library**: `org.dhatim:fastexcel` (writer) + `org.dhatim:fastexcel-reader` (reader).
- **Forbidden**: `com.alibaba:easyexcel*` is rejected at Gradle resolution time (see root `build.gradle.kts`). Direct use of `org.apache.poi:*` is also discouraged — go through `FastExcelUtil` in `common-core`.
- **Streaming**: FastExcel is streaming by default — large workbooks must not be materialized in memory. Pass an `OutputStream` to `FastExcelUtil.write(out, sheetName, headers, rows)` and stream rows from a `Stream`/`Iterable`.
- **Headers**: Always include a header row; readers should skip it via `FastExcelUtil.readFirstSheet(in)` (skips row 0 by convention).

```java
@GetMapping("/user/export")
public void export(HttpServletResponse response) throws IOException {
    response.setContentType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition", "attachment; filename=users.xlsx");
    userExportService.exportTo(response.getOutputStream());
}
```

### 3.8 Scheduling (Quartz)

- **Library**: `org.springframework.boot:spring-boot-starter-quartz` (Quartz 2.5.x).
- **JobStore**: JDBC (`LocalDataSourceJobStore`) over the existing PostgreSQL master DataSource, **clustered** (`org.quartz.jobStore.isClustered=true`). Schema is the upstream `tables_postgres.sql` plus ArchSmith metadata tables `sys_quartz_job` and `sys_quartz_log` (created by `V4__quartz_schema.sql`).
- **Reflective dispatch pattern**: a single Quartz `Job` class — `QuartzReflectionJob` — reads `beanName`/`methodName`/`methodParams` from the trigger `JobDataMap`, resolves the Spring bean via `ApplicationContext`, invokes the method by reflection (arity-matched), and persists a `SysQuartzLog` row capturing duration and any error. New scheduled tasks therefore require only a metadata row and a Spring bean — **no new `Job` class per task**.
- **Method params**: stored as a JSON array of primitives (`["foo", 42, true]`) for transparency.
- **REST surface** (`server-admin`):
  - `POST /quartz/list` paged query · `POST /quartz/add` · `PUT /quartz/update/{id}` · `DELETE /quartz/delete/{id}`
  - `POST /quartz/pause/{id}` · `POST /quartz/resume/{id}` · `POST /quartz/run/{id}` (one-shot trigger)
  - `POST /quartz/log/list` · `POST /quartz/validate-cron`
- **UI**: `AppForgeAdmin` → System → 定时任务 (`/system/quartz/index`).

```java
@Component("demoQuartzJob")
@Slf4j
public class DemoQuartzJobBean {
    public void helloWorld() { log.info("hello @ {}", Instant.now()); }
}
// Persist a SysQuartzJob row { beanName: "demoQuartzJob", methodName: "helloWorld", cron: "0/30 * * * * ?" }
// → QuartzReflectionJob fires it on schedule, no extra Java class needed.
```

---

## 4. Configuration Standards

### 4.1 Custom Config Prefix

All application-specific configuration lives under the `arch-smith` prefix:

```yaml
arch-smith:
  name: ArchSmith
  version: 1.0.0
  token:
    header: Authorization
    auto-refresh-time: 20
  file-storage:
    type: local          # local | s3
    local-dir: uploads
  jwt:
    secret: ${JWT_SECRET}
    expire-seconds: 604800
```

### 4.2 Profile Strategy

| Profile | Purpose | Database | Redis | File Storage |
|---------|---------|----------|-------|-------------|
| `dev` | Local development | Testcontainers PostgreSQL | Embedded or local | Local filesystem |
| `test` | CI / staging | Real PostgreSQL | Real Redis | S3-compatible |
| `prod` | Production | Real PostgreSQL (master/slave) | Real Redis cluster | S3 |

File layout:

```
server-<name>/src/main/resources/
├── application.yaml                  # Shared base config (active profile defaults to dev)
├── application-dev.yaml              # Dev: Testcontainers, DevTools, 100% trace sampling
├── application-test.yaml.example     # Template for test environment
├── application-prod.yaml.example     # Template for production
└── log4j2-spring.xml                 # Single file with <SpringProfile> sections
```

- `application-test.yaml` and `application-prod.yaml` are **gitignored** — never commit real credentials.
- Set active profile via `SPRING_PROFILES_ACTIVE` env var or `-Dspring.profiles.active=...`.

### 4.3 Secrets Management

- **Never** store secrets in source control.
- Use environment variables for sensitive values: `${JWT_SECRET}`, `${DB_PASSWORD}`, etc.
- Dev profile may use hardcoded values for local convenience (Testcontainers auto-generates credentials).
- Production uses environment injection from orchestrator (Docker Compose, Kubernetes, etc.).

### 4.4 Datasource Configuration

Multi-datasource via `dynamic-datasource-spring-boot4-starter`:

```yaml
spring:
  datasource:
    dynamic:
      primary: user_master
      strict: false
      datasource:
        user_master:
          driver-class-name: org.postgresql.Driver
          url: jdbc:postgresql://${DB_HOST}:5432/archsmith_user
          username: ${DB_USER}
          password: ${DB_PASSWORD}
        user_slave:
          driver-class-name: org.postgresql.Driver
          url: jdbc:postgresql://${DB_SLAVE_HOST}:5432/archsmith_user
          username: ${DB_USER}
          password: ${DB_PASSWORD}
```

- `GroupDataSourceProxy` bridges dynamic-datasource groups with JPA `EntityManagerFactory` instances.
- Use `@DS("group_name")` for explicit datasource routing when needed.

### 4.5 Logging

Single `log4j2-spring.xml` with Spring profile sections:

- **Dev**: Console appender (colorized), DEBUG level for application packages.
- **Non-dev**: File appender only (rolling, compressed), INFO level. No console output.

---

## 5. Testing Standards

### 5.1 Test Frameworks

| Framework | Purpose | Scope |
|-----------|---------|-------|
| **JUnit 6** (Jupiter 6.x) | Unit tests, assertions | All modules |
| **Spock 2.4** (Groovy 5.x) | BDD-style specs, data-driven tests | All modules |
| **Testcontainers** | Integration tests with real services | Server modules |
| **RestClient** | API integration tests | Server modules |
| **Spring Boot Test** | Context loading, sliced tests | Server / infrastructure |

### 5.2 Test Organization

```
src/test/
├── java/           # JUnit 6 tests
│   └── com/lesofn/archsmith/...
│       ├── unit/       # Pure unit tests (no Spring context)
│       └── integration/ # @SpringBootTest, Testcontainers
└── groovy/         # Spock specifications
    └── com/lesofn/archsmith/...
        └── UserServiceSpec.groovy
```

### 5.3 Naming Conventions

- JUnit: `<Class>Test.java` (e.g., `UserServiceTest.java`)
- Spock: `<Class>Spec.groovy` (e.g., `UserServiceSpec.groovy`)
- Integration: `<Class>IntegrationTest.java` or `<Class>IT.java`

### 5.4 Testcontainers Usage

Dev profile auto-starts Testcontainers for PostgreSQL, Redis, and RustFS via embedded configuration flags:

```yaml
arch-smith:
  embedded:
    redis: true
    postgresql: true
    db-init: true
    s3: true
```

For integration tests, use `@Testcontainers` annotation with shared containers:

```java
@SpringBootTest
@Testcontainers
class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.dynamic.datasource.user_master.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.dynamic.datasource.user_master.username", postgres::getUsername);
        registry.add("spring.datasource.dynamic.datasource.user_master.password", postgres::getPassword);
    }
}
```

### 5.5 Gate Rule

**`./gradlew build` must pass before every commit.** This runs:

1. `spotlessCheck` — code formatting
2. `compileJava` — compilation with `-Xlint:deprecation` and `--enable-preview`
3. `test` — all unit and integration tests

---

## 6. Deployment Standards

### 6.1 Docker Multi-Stage Build (JVM Mode)

The standard production image uses a two-stage Dockerfile:

**Stage 1: Training** (based on `azul/zulu-openjdk:25`)
- Copy the fat JAR (`server-<name>/build/libs/server-<name>.jar`)
- Generate Project Leyden CDS/AOT cache (`-XX:AOTMode=record` then `-XX:AOTMode=create`)
- Run `jlink` to produce a minimal JRE with only required modules

**Stage 2: Runtime** (based on `alpine:3.21`)
- Copy minimal JRE, JAR, and AOT cache from training stage
- Run as non-root user (`archsmith:1001`)
- JVM flags: `UseZGC`, `UseCompactObjectHeaders`, `MaxRAMPercentage=75%`, AOT cache enabled

### 6.2 JVM Runtime Flags

```
-XX:AOTCache=app.aot
-XX:+UseCompactObjectHeaders
-XX:+UseZGC
-XX:MaxRAMPercentage=75.0
-XX:InitialRAMPercentage=50.0
--enable-preview
--enable-native-access=ALL-UNNAMED
-Djava.security.egd=file:/dev/./urandom
```

### 6.3 Docker Compose Stack

```
docker/
├── docker-compose.yml          # JVM mode
├── docker-compose.native.yml   # Native Image mode
├── start.sh                    # ./start.sh jvm | ./start.sh native
├── init-db.sql                 # Database initialization
├── jvm/Dockerfile
├── native/Dockerfile
└── nginx/                      # Reverse proxy config
```

Full stack includes: PostgreSQL + Redis + Application + Nginx reverse proxy.

### 6.4 Database Migrations

- Flyway manages all schema changes.
- Migration files: `V<version>__<description>.sql` (e.g., `V1__create_user_table.sql`)
- Location: `server-<name>/src/main/resources/db/migration/`
- Flyway runs automatically on application startup (can be disabled per profile).

### 6.5 Health Checks

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | Liveness / readiness probe |
| `/actuator/metrics` | Application metrics |
| `/actuator/prometheus` | Prometheus scrape endpoint |
| `/actuator/info` | Application info |

Exposed via management configuration:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### 6.6 Observability

- **Tracing**: Micrometer Tracing bridge to OpenTelemetry, OTLP exporter.
- **Metrics**: Micrometer with Prometheus endpoint + OTLP metrics export.
- **Sampling**: 100% in dev, 10% in prod (configurable via `OTEL_TRACES_SAMPLER_ARG`).
- **Endpoint**: Configurable via `OTEL_EXPORTER_OTLP_ENDPOINT` environment variable.

---

## 7. Dependency Management

### 7.1 BOM Structure

All third-party versions are declared in `dependencies/build.gradle.kts` as a `java-platform`:

```kotlin
plugins {
    `java-platform`
}

javaPlatform { allowDependencies() }

dependencies {
    constraints {
        api("com.google.guava:guava:33.4.8-jre")
        api("org.mapstruct:mapstruct:1.6.3")
        // ... all versions here
    }
}
```

Every subproject imports both the Spring Boot BOM and the project BOM:

```kotlin
dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.5"))
    implementation(platform(project(":dependencies")))
}
```

### 7.2 Adding a New Dependency

1. Add the version constraint to `dependencies/build.gradle.kts`
2. Reference the dependency **without version** in the consuming module's `build.gradle.kts`
3. Never specify versions in individual module build files

---

## 8. Checklist for New Services

- [ ] Module follows the standard structure (`server-<name>`, `domain/<context>`)
- [ ] `settings.gradle.kts` updated with new module includes
- [ ] `dependencies/build.gradle.kts` updated if new libraries introduced
- [ ] `package-info.java` with `@NullMarked` in every package
- [ ] Spotless configured (inherited from root `build.gradle.kts`)
- [ ] JDK 25 toolchain configured (inherited from root)
- [ ] `application.yaml` uses `arch-smith` prefix for custom config
- [ ] Profile YAML files created (dev at minimum)
- [ ] Secrets use environment variables, not hardcoded values
- [ ] Flyway migrations for all database schema
- [ ] Testcontainers for dev profile database/cache
- [ ] Unit tests (JUnit 6 or Spock) for business logic
- [ ] Integration tests with Testcontainers for repositories
- [ ] API integration tests using RestClient
- [ ] Dockerfile follows the jlink + Leyden CDS pattern
- [ ] `docker-compose.yml` updated with new service
- [ ] Actuator health/metrics endpoints exposed
- [ ] `./gradlew build` passes clean
