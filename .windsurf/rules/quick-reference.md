# Quick Reference for AppBoot Development

## Most Important Rules

### 🚫 NEVER DO THIS
- **NEVER use `var` or `val`** - always explicit types
- **NEVER use `@Value`** - use AppBootConfig bean
- **NEVER import hutool** - use standard libraries
- **NEVER use field injection** - use constructor injection

### ✅ ALWAYS DO THIS
- **ALWAYS use Lombok annotations** for boilerplate reduction
- **ALWAYS use AppBootConfig** for configuration access
- **ALWAYS use constructor injection** with `@RequiredArgsConstructor`
- **ALWAYS specify explicit types** in variable declarations

## Quick Command Reference

```bash
# Build project
./gradlew clean build

# Run application
./gradlew server-admin:bootRun

# Run tests
./gradlew test

# Run specific module
./gradlew :server-admin:test
```

## Configuration Quick Access

```java
// Inject configuration
@Service
@RequiredArgsConstructor
public class MyService {
    private final AppBootConfig config;
    
    // Access configuration
    public void example() {
        String header = config.getToken().getHeader();
        boolean captcha = config.getCaptcha().isEnabled();
        String jwtSecret = config.getJwt().getSecret();
    }
}
```

## Lombok Quick Patterns

### Service/Component Class
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository repository;
    private final AppBootConfig config;
}
```

### DTO/Data Class
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
}
```

### Entity Class
```java
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends IdEntity {
    private String username;
    private String email;
}
```

## Module Structure Reference

```
AppBoot/
├── common/
│   ├── common-core/      # Utilities, constants
│   └── common-error/      # Error handling
├── domain/
│   └── admin-user/        # User domain logic
├── infrastructure/        # Config, auth, cross-cutting
├── server-admin/          # Web layer, controllers
├── dependencies/          # Version management
└── .windsurf/rules/       # Project rules (this file)
```

## Environment URLs

### Development
- Application: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- H2 Console: http://localhost:8080/h2-console
- Health: http://localhost:7002/health
- Metrics: http://localhost:7002/metrics
- Druid: http://localhost:8080/druid

## Common Patterns

### REST Controller
```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;
    
    @GetMapping("/{id}")
    public Result<UserDTO> get(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }
}
```

### Error Handling
```java
throw new ApiException(ErrorCode.USER_NOT_FOUND, "User not found: " + id);
```

### Logging
```java
@Slf4j  // Lombok annotation
public class MyClass {
    public void method() {
        log.info("Info message: {}", variable);
        log.error("Error occurred", exception);
    }
}
```

## Configuration Files

- Common: `application-base.yaml`
- Dev: `profiles/dev/application.yaml`
- Test: `profiles/test/application.yaml`
- Prod: `profiles/prod/application.yaml`

## Security Headers

```java
// JWT Token header
Authorization: Bearer {token}
```

## Database Access Pattern

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

---
Remember: When in doubt, check `AppBootConfig` for configuration and use Lombok for cleaner code!