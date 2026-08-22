# ArchForge Project Standard

> Canonical reference for technology choices, module layout, code conventions, configuration, testing, and deployment across all ArchForge services.

---

## 1. Tech Stack

| Category | Technology | Version | Notes |
|----------|-----------|---------|-------|
| **Language** | Java (Azul Zulu) | 25 | Preview features enabled (`--enable-preview`) |
| **Framework** | Spring Boot | 4.x (currently 4.1.0) | Virtual threads enabled by default |
| **Build** | Gradle (Kotlin DSL) | 9.x (currently 9.5.1) | Configuration cache supported |
| **Database** | PostgreSQL | 17 | All environments; Testcontainers in dev |
| **Cache** | Redis | 7 | Lettuce client via Spring Data Redis |
| **Migrations** | Flyway | 12.x | `flyway-database-postgresql` dialect |
| **ORM** | Spring Data JPA + Hibernate | (Boot-managed) | `CamelCaseToUnderscores` naming strategy |
| **Multi-Datasource** | dynamic-datasource-spring-boot4-starter | 4.5.x | Master/slave groups, `@DS` annotation |
| **DTO Mapping** | MapStruct | 1.6.x | Compile-time code generation |
| **Null Safety** | JSpecify | 1.0.x | `@NullMarked` on every package |
| **API Docs** | SpringDoc OpenAPI | 2.8.x | Swagger UI at `/swagger-ui/index.html` |
| **Auth** | sa-token | 1.45.0 | `StpAdminUtil` / `StpWebUtil`; `@SaCheckLogin` / `@SaCheckPermission` |
| **Observability** | Micrometer + OpenTelemetry | (Boot-aligned) | OTLP export; 100% sampling dev, 10% prod |
| **Logging** | Log4j2 | (Boot-managed) | `log4j2-spring.xml` with `<SpringProfile>` |
| **Object Storage** | AWS S3 SDK | 2.46.8 | `FileStorageService` abstraction (local/s3) |
| **Code Style** | Spotless + Google Java Style (AOSP) | Plugin 8.4.x, GJF 1.35.x | 4-space indent, auto-enforced |
| **Testing** | JUnit 6 + Spock 2.4 (Groovy 5.x) | See BOM | Testcontainers for integration |
| **Containerization** | Docker (multi-stage) | — | jlink minimal JRE + Project Leyden CDS/AOT |

### Prohibited Dependencies

- **`cn.hutool:hutool-all`** — Use JDK standard library, Apache Commons, Guava, or Spring utilities instead.
- **Logback** — Excluded globally; use Log4j2.
- **`var` keyword** — Always use explicit types for readability and team consistency.
- **`com.alibaba:easyexcel*`** — Forbidden by Gradle build guard (root `build.gradle.kts`). The project standard Excel I/O library is `org.dhatim:fastexcel` + `fastexcel-reader`. See §3.7 Excel I/O.
- **`org.apache.poi:*` (direct use)** — Pulled in only as a FastExcel transitive when strictly needed. Application code should use `FastExcelUtil` (in `common-base`) rather than POI APIs.

---

## 2. Module Structure

```
ArchForge/
├── archforge-dependencies/        # Centralized java-platform BOM
│   └── build.gradle.kts           # All third-party version constraints
├── archforge-common/
│   ├── archforge-common-base/     # Shared utilities, base entities, constants
│   ├── archforge-common-error/    # Error codes, exception hierarchy
│   └── archforge-common-jpa/      # JPA base entities, GroupDataSourceProxy
├── archforge-infrastructure/      # Auth (sa-token), filters, file storage
├── archforge-domain/
│   └── archforge-<bounded-context>/  # e.g. admin-user, blog, meta-table
├── archforge-server-admin/        # Admin API :8080
├── archforge-server-web/          # C-end API :8081
├── archforge-cli/
├── archforge-example/             # Example/demo modules
│   └── archforge-example-task/
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
archforge-server-* → archforge-domain/* → archforge-common/archforge-common-base
archforge-server-* → archforge-infrastructure → archforge-common/{base,error,jpa}
```

- **`archforge-common-base`**: No Spring dependencies. Pure Java utilities, base classes, constants.
- **`archforge-common-error`**: Error code enums, exception base classes, standard API response wrappers.
- **`archforge-infrastructure/`**: Spring-aware cross-cutting concerns — sa-token, filters, file storage, observability.
- **`archforge-domain/<context>/`**: Business logic with DDD patterns. Depends on common-base/jpa.
- **`archforge-server-*`**: Thin web layer — controllers, DTOs, Spring Boot entry point.
- **`archforge-dependencies/`**: `java-platform` BOM. Every other module applies `platform(project(":archforge-dependencies"))`.

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
package com.lesofn.archforge.domain.user;

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
- **Forbidden**: `com.alibaba:easyexcel*` is rejected at Gradle resolution time (see root `build.gradle.kts`). Direct use of `org.apache.poi:*` is also discouraged — go through `FastExcelUtil` in `common-base`.
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
- **JobStore**: JDBC (`LocalDataSourceJobStore`) over the existing PostgreSQL master DataSource, **clustered** (`org.quartz.jobStore.isClustered=true`). Schema is the upstream `tables_postgres.sql` plus ArchForge metadata tables `sys_quartz_job` and `sys_quartz_log` (created by `V4__quartz_schema.sql`).
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

### 3.9 Query Pattern (Declarative JPA Filters)

For paged search endpoints, prefer **declarative criteria DTOs** over hand-rolled
`Specification` lambdas.

**Components:**

- **`@Query`** (`com.lesofn.archforge.common.annotation.Query`) — field-level annotation
- **`QueryHelp.getPredicate(root, criteria, cb)`** (`com.lesofn.archforge.common.utils.query.QueryHelp`) — reflection-driven predicate builder
- **Repository** must `extends JpaSpecificationExecutor<T>`

**Quick example:**

```java
// 1. Annotate the criteria DTO
@Data
public class SysUserQueryRequest {
    @Query(blurry = "username,nickname,email") private String blurry;
    @Query(type = Query.Type.INNER_LIKE)       private String username;
    @Query                                      private Integer status;
    @Query(type = Query.Type.BETWEEN)          private List<LocalDateTime> createTime;
    @Query(propName = "id", type = Query.Type.IN, joinName = "dept")
    private Set<Long> deptIds;
}

// 2. Build a Specification from the criteria in the controller
Specification<SysUser> spec = (root, q, cb) -> QueryHelp.getPredicate(root, criteria, cb);
Page<SysUser> page = userRepository.findAll(spec, pageable);
```

**Supported operators (`Query.Type`):**

| Operator | SQL equivalent |
|---|---|
| `EQUAL` | `= value` |
| `NOT_EQUAL` | `<> value` |
| `GREATER_THAN` | `>= value` (inclusive) |
| `LESS_THAN` | `<= value` (inclusive) |
| `LESS_THAN_NQ` | `< value` (strict) |
| `INNER_LIKE` | `LIKE '%value%'` |
| `LEFT_LIKE` | `LIKE '%value'` |
| `RIGHT_LIKE` | `LIKE 'value%'` |
| `IN` | `IN (collection)` |
| `NOT_IN` | `NOT IN (collection)` |
| `IS_NULL` | `IS NULL` |
| `NOT_NULL` | `IS NOT NULL` |
| `BETWEEN` | `BETWEEN bounds[0] AND bounds[1]` (value must be a `List` of size 2) |
| `FIND_IN_SET` | `FIND_IN_SET(value, column) > 0` |

**Blurry multi-field LIKE:** set `blurry = "fieldA,fieldB"` on a `String` field; the field
value is wrapped as `(fieldA LIKE %v% OR fieldB LIKE %v%)`. The `type` attribute is ignored
when `blurry` is non-empty.

**Joins:** `joinName = "dept"` performs a LEFT JOIN (configurable via `join = Query.Join.INNER`
etc.); nested paths use `>` separator (e.g. `joinName = "dept>parent"`). Joins are cached
per-invocation so repeating the same `joinName` reuses the join.

**Empty values are silently skipped** — `null`, blank `String`, and empty `Collection`/`array`
contribute no predicate. This means a fully-null criteria object returns an unconditional
`AND()` (select all).

**Boundary between layers:** the criteria DTO lives in `server-admin` (transport concern).
`SysUserService.findAll(Specification<SysUser>, Pageable)` accepts a `Specification` so
`domain/admin-user` has no dependency on `server-admin` types. The translation from DTO →
`Specification` happens in the controller.

---

## 4. Configuration Standards

### 4.1 Custom Config Prefix

All application-specific configuration lives under the `arch-forge` prefix:

```yaml
arch-forge:
  name: ArchForge
  version: 1.0.0
  token:
    header: Authorization
    auto-refresh-time: 20
  file-storage:
    type: local          # local | s3
    local-dir: uploads
  sa-token:
    token-name: Authorization
    timeout: 604800
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

server-<name>/src/test/resources/
└── application-test.yaml             # Committed test profile (Testcontainers + Flyway), never ships in the prod jar
```

- `application-test.yaml` lives in `src/test/resources` (committed, test classpath only).
- `application-prod.yaml` is **gitignored** — never commit real credentials.
- Set active profile via `SPRING_PROFILES_ACTIVE` env var or `-Dspring.profiles.active=...`.

### 4.3 Secrets Management

- **Never** store secrets in source control.
- Use environment variables for sensitive values: `${DB_PASSWORD}`, `${DRUID_PASSWORD}`, sa-token secrets, etc.
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
          url: jdbc:postgresql://${DB_HOST}:5432/archforge_user
          username: ${DB_USER}
          password: ${DB_PASSWORD}
        user_slave:
          driver-class-name: org.postgresql.Driver
          url: jdbc:postgresql://${DB_SLAVE_HOST}:5432/archforge_user
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
│   └── com/lesofn/archforge/...
│       ├── unit/       # Pure unit tests (no Spring context)
│       └── integration/ # @SpringBootTest, Testcontainers
└── groovy/         # Spock specifications
    └── com/lesofn/archforge/...
        └── UserServiceSpec.groovy
```

### 5.3 Naming Conventions

- JUnit: `<Class>Test.java` (e.g., `UserServiceTest.java`)
- Spock: `<Class>Spec.groovy` (e.g., `UserServiceSpec.groovy`)

### 5.4 Test Data Builders

Cross-module test data builders live in `src/testFixtures/java/<module>.testing/` and are
consumed via `testImplementation(testFixtures(project(...)))`:

| Builder | Module | Entity |
|---------|--------|--------|
| `UserTestBuilder` / `RoleTestBuilder` | archforge-admin-user | `SysUser` / `SysRole` |
| `ArticleTestBuilder` | archforge-blog | `BlogArticle` |
| `MetaTableTestBuilder` | archforge-meta-table | `MetaTable` + `MetaColumn` |
| `ChatSessionTestBuilder` | archforge-server-admin (test) | chat session message maps |

Rules:

- Builders produce **valid-by-default** entities; tests only configure fields they assert on.
- Unique columns (username, slug, tableCode...) get an auto-increment suffix by default.
- Never encode passwords inside builders — pass already-encoded values via
  `withEncodedPassword(...)`.
- New core entities get a builder before their first service test.

### 5.5 Test Tags (@Tag)

| Tag | Meaning | Examples |
|-----|---------|----------|
| `P0` | Security-critical / money-path unit tests | login, permission checks |
| `P1` | Regular business logic tests | CRUD services |
| `contract` | Contract/architecture enforcement | enum contract, permission coverage, ArchUnit |
| `slow` | Container-backed integration tests | @SpringBootTest + Testcontainers |

Filtering from the CLI:

```bash
./gradlew test -Ptags=P0,contract     # only these tags
./gradlew build -PexcludeTags=slow    # skip slow integration tests
```

Every `@SpringBootTest` must be tagged `slow`. New contract tests must be tagged `contract`.
- Integration: `<Class>IntegrationTest.java` or `<Class>IT.java`

### 5.4 Testcontainers Usage

Dev profile auto-starts Testcontainers for PostgreSQL, Redis, and RustFS via embedded configuration flags:

```yaml
arch-forge:
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
- Run as non-root user (`archforge:1001`)
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

- **Tracing**: Micrometer Tracing bridge to OpenTelemetry, OTLP exporter to Jaeger.
- **Metrics**: Micrometer with Prometheus scrape endpoint (`/actuator/prometheus`).
- **Dashboards**: Pre-configured Grafana dashboards for JVM, HTTP, and system metrics.
- **Alerts**: Prometheus alert rules for error rate, latency, JVM heap, CPU, disk, and availability.
- **Sampling**: Configurable via `SAMPLING_PROBABILITY` environment variable (default 1.0 in dev, 0.1 in production Docker Compose).
- **Endpoint**: Configurable via `OTEL_EXPORTER_OTLP_ENDPOINT` environment variable (default `http://localhost:4318/v1/traces`).

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
        api("com.google.guava:guava:33.6.0-jre")
        api("org.mapstruct:mapstruct:1.6.3")
        // ... all versions here
    }
}
```

Every subproject imports both the Spring Boot BOM and the project BOM:

```kotlin
dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.1.0"))
    implementation(platform(project(":archforge-dependencies")))
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
- [ ] `application.yaml` uses `arch-forge` prefix for custom config
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
