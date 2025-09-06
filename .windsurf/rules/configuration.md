# Configuration Management Rules

## Configuration Structure

### Application Configuration Hierarchy

```
application-basic.yaml     # Common configurations
├── application-dev.yaml   # Development environment
├── application-test.yaml  # Test environment
└── application-prod.yaml  # Production environment
```

## AppBootConfig Bean Usage

### Configuration Bean Structure
```java
@ConfigurationProperties(prefix = "app-boot")
public class AppBootConfig {
    private String name;
    private String version;
    private Token token = new Token();
    private Jwt jwt = new Jwt();
    private Captcha captcha = new Captcha();
    
    @Getter @Setter
    public static class Token {
        private String header = "Authorization";
        private int autoRefreshTime = 20;
    }
    
    @Getter @Setter
    public static class Jwt {
        private String secret;
        private long expireSeconds = 604800;
    }
    
    @Getter @Setter
    public static class Captcha {
        private boolean enabled = true;
    }
}
```

### ✅ CORRECT - Using AppBootConfig
```java
@Service
@RequiredArgsConstructor
public class TokenService {
    private final AppBootConfig appBootConfig;
    
    public String getTokenHeader() {
        return appBootConfig.getToken().getHeader();
    }
    
    public boolean isCaptchaEnabled() {
        return appBootConfig.getCaptcha().isEnabled();
    }
}
```

### ❌ WRONG - Using @Value directly
```java
@Service
public class TokenService {
    @Value("${token.header}")  // DON'T DO THIS
    private String header;
    
    @Value("${captcha.enabled}")  // DON'T DO THIS
    private boolean captchaEnabled;
}
```

## YAML Configuration Guidelines

### Common Configuration (application-basic.yaml)
```yaml
# Server configuration
server:
  port: 8080
  servlet:
    encoding:
      charset: UTF-8

# Management endpoints
management:
  server:
    port: 7002

# Application properties
app-boot:
  name: AppBoot
  version: 1.0.0
  token:
    header: Authorization
    auto-refresh-time: 20
```

### Environment-Specific Configuration

#### Development (application-dev.yaml)
```yaml
spring:
  profiles:
    active: basic,dev
  devtools:
    restart:
      enabled: true
  datasource:
    h2:
      console:
        enabled: true

app-boot:
  jwt:
    secret: dev-secret-key
    expire-seconds: 604800  # 7 days
  captcha:
    enabled: false  # Disabled for easier testing
```

#### Production (application-prod.yaml)
```yaml
spring:
  profiles:
    active: basic,prod

app-boot:
  jwt:
    secret: ${JWT_SECRET}  # From environment variable
    expire-seconds: 3600   # 1 hour
  captcha:
    enabled: true
```

## Configuration Best Practices

### 1. Centralize Configuration Access
- All configuration access should go through `AppBootConfig`
- Never scatter `@Value` annotations throughout the codebase
- Create nested configuration classes for logical grouping

### 2. Environment Variable Support
```yaml
# Production configuration with env variable fallback
database:
  url: ${DATABASE_URL:jdbc:mysql://localhost:3306/appboot}
  username: ${DB_USERNAME:root}
  password: ${DB_PASSWORD:}
  
jwt:
  secret: ${JWT_SECRET:default-secret-key}
```

### 3. Configuration Validation
```java
@ConfigurationProperties(prefix = "app-boot")
@Validated
public class AppBootConfig {
    
    @NotBlank
    private String name;
    
    @Valid
    @NotNull
    private Jwt jwt;
    
    public static class Jwt {
        @NotBlank
        private String secret;
        
        @Min(60)
        private long expireSeconds;
    }
}
```

### 4. Configuration Documentation
```java
public class AppBootConfig {
    
    /**
     * Application name displayed in UI and logs
     */
    private String name;
    
    /**
     * JWT configuration for authentication
     */
    private Jwt jwt;
    
    public static class Jwt {
        /**
         * Secret key for JWT signing (Base64 encoded)
         */
        private String secret;
        
        /**
         * Token expiration time in seconds
         * Dev: 604800 (7 days)
         * Prod: 3600 (1 hour)
         */
        private long expireSeconds;
    }
}
```

## Migration Guide

### Converting @Value to AppBootConfig

#### Step 1: Identify @Value usage
```bash
grep -r "@Value" --include="*.java" .
```

#### Step 2: Add to AppBootConfig
```java
// Add new configuration property
public class AppBootConfig {
    private NewConfig newConfig = new NewConfig();
    
    @Getter @Setter
    public static class NewConfig {
        private String property;
    }
}
```

#### Step 3: Update YAML files
```yaml
app-boot:
  new-config:
    property: value
```

#### Step 4: Replace @Value with bean injection
```java
// Before
@Value("${new-config.property}")
private String property;

// After
private final AppBootConfig appBootConfig;

public String getProperty() {
    return appBootConfig.getNewConfig().getProperty();
}
```

## Configuration Loading Order

1. `application.yaml` (if exists)
2. `application-basic.yaml`
3. `application-{profile}.yaml`
4. Environment variables
5. Command line arguments

## Security Considerations

### Sensitive Configuration
- Never commit production secrets to version control
- Use environment variables for sensitive data
- Provide sensible defaults for development
- Document required environment variables

### Example Secure Configuration
```yaml
# Development - hardcoded safe values
jwt:
  secret: dev-only-secret-key

# Production - from environment
jwt:
  secret: ${JWT_SECRET}  # Must be provided via env

# With fallback (use carefully)
redis:
  password: ${REDIS_PASSWORD:}  # Empty default for dev
```

## Troubleshooting

### Common Issues

1. **Configuration not loading**
   - Check profile activation: `spring.profiles.active`
   - Verify YAML indentation
   - Check property naming convention (kebab-case in YAML, camelCase in Java)

2. **@ConfigurationProperties not working**
   - Ensure `@EnableConfigurationProperties` or `@ConfigurationPropertiesScan`
   - Add spring-boot-configuration-processor dependency
   - Check prefix spelling

3. **Environment variable not picked up**
   - Verify variable is exported
   - Check placeholder syntax: `${VAR_NAME}`
   - Use fallback values: `${VAR_NAME:default}`