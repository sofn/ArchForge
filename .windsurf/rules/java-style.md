# Java Code Style Rules

## Lombok Usage Guidelines

### Required Practices
1. **Use Lombok annotations actively** to reduce boilerplate code
2. **Always use explicit type declarations** - NEVER use `var` or `val`
3. **Prefer constructor injection** with `@RequiredArgsConstructor`

### Recommended Lombok Annotations

#### For Data Classes
```java
@Data  // Generates getters, setters, toString, equals, hashCode
@Builder  // Generates builder pattern
@NoArgsConstructor  // Default constructor
@AllArgsConstructor  // Constructor with all fields
```

#### For Spring Components
```java
@Component
@RequiredArgsConstructor  // Constructor injection
@Slf4j  // Logging
public class MyService {
    private final MyRepository repository;  // final for constructor injection
}
```

#### For DTOs and Entities
```java
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)  // When extending a base class
public class UserDTO extends BaseDTO {
    private String username;
    private String email;
}
```

### Prohibited Practices

#### ❌ DO NOT use var/val
```java
// WRONG
var list = new ArrayList<String>();
val result = someMethod();

// CORRECT
List<String> list = new ArrayList<>();
String result = someMethod();
```

#### ❌ DO NOT use field injection
```java
// WRONG
@Autowired
private MyService myService;

// CORRECT
private final MyService myService;  // With @RequiredArgsConstructor
```

## General Java Guidelines

### Import Rules
- ✅ Use standard JDK utilities
- ✅ Use Apache Commons libraries
- ✅ Use Guava utilities
- ✅ Use Spring utilities
- ❌ **NEVER** import `cn.hutool:hutool-all`

### Type Declaration
- Always use explicit types
- Use interface types for collections (`List<>` not `ArrayList<>`)
- Use meaningful variable names

### Method Guidelines
- Keep methods short and focused (single responsibility)
- Use descriptive method names
- Document complex logic with comments
- Return early to reduce nesting

### Exception Handling
- Use custom exceptions for business logic errors
- Always log exceptions with appropriate level
- Provide meaningful error messages
- Use try-with-resources for auto-closeable resources

## Spring Boot Best Practices

### Dependency Injection
```java
// CORRECT - Constructor injection with Lombok
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final AppBootConfig appBootConfig;
}
```

### Configuration Management
```java
// CORRECT - Use configuration beans
@Component
public class MyComponent {
    private final AppBootConfig appBootConfig;
    
    public MyComponent(AppBootConfig appBootConfig) {
        this.appBootConfig = appBootConfig;
    }
    
    public void doSomething() {
        String token = appBootConfig.getToken().getHeader();
        // Use configuration
    }
}
```

### REST Controller Pattern
```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    
    @GetMapping("/{id}")
    public Result<UserDTO> getUser(@PathVariable Long id) {
        log.info("Getting user with id: {}", id);
        return Result.success(userService.getUser(id));
    }
}
```

## Code Organization

### Package Structure
- `controller` - REST endpoints
- `service` - Business logic
- `repository` - Data access
- `dto` - Data transfer objects
- `entity/domain` - Domain entities
- `config` - Configuration classes
- `util` - Utility classes
- `exception` - Custom exceptions

### Class Naming Conventions
- Controllers: `*Controller`
- Services: `*Service`
- Repositories: `*Repository`
- DTOs: `*DTO` or `*Command` or `*Query`
- Entities: Plain names (e.g., `User`, `Role`)
- Configuration: `*Config`
- Utils: `*Util` or `*Utils`

## Testing Guidelines

### Test Class Naming
- Unit tests: `*Test`
- Integration tests: `*IntegrationTest`
- Controller tests: `*ControllerTest`

### Test Structure
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void shouldCreateUser() {
        // Given
        UserDTO userDTO = createUserDTO();
        
        // When
        User result = userService.createUser(userDTO);
        
        // Then
        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }
}
```

## Database and JPA

### Entity Guidelines
```java
@Entity
@Table(name = "sys_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysUser extends IdEntity {
    
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    
    @Column(name = "email")
    private String email;
    
    // Avoid @Data for entities to prevent issues with lazy loading
}
```

### Repository Pattern
```java
@Repository
public interface UserRepository extends JpaRepository<SysUser, Long> {
    
    Optional<SysUser> findByUsername(String username);
    
    @Query("SELECT u FROM SysUser u WHERE u.status = :status")
    List<SysUser> findByStatus(@Param("status") Integer status);
}
```