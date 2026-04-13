# Dynamic Datasource Migration Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the custom read-write datasource routing (ReadWriteDataSourceRouter + DataBaseAdvisor + manual Druid config) with `dynamic-datasource-spring-boot4-starter`, and remove the incompatible `druid-spring-boot-3-starter`.

**Architecture:** The project has two database groups (user, task), each with master/slave pairs and separate JPA EntityManagerFactories. The migration introduces `dynamic-datasource-spring-boot4-starter` which manages all datasource definitions via YAML config and provides `DynamicRoutingDataSource`. A lightweight `GroupDataSourceProxy` bridges between the DynamicRoutingDataSource and each EntityManagerFactory, ensuring each EMF always connects to the correct database group. Custom read-write split AOP is removed; explicit `@DS` annotations or `@Transactional(readOnly=true)` can be used when needed.

**Tech Stack:** Spring Boot 4.0.5, dynamic-datasource-spring-boot4-starter 4.5.0, Druid (pooled by dynamic-datasource), JPA/Hibernate 7, H2 (dev), MySQL (prod)

---

## File Map

### New Files
- `infrastructure/src/main/java/com/lesofn/appforge/infrastructure/frame/database/GroupDataSourceProxy.java` - DataSource proxy that pushes a group name before delegating to DynamicRoutingDataSource

### Modified Files
- `dependencies/build.gradle.kts` - Replace druid deps with dynamic-datasource
- `common/common-core/build.gradle.kts` - Remove `com.alibaba:druid`
- `server-admin/build.gradle.kts` - Remove `druid-spring-boot-3-starter`
- `server-admin/src/main/java/com/lesofn/appforge/server/admin/Application.java` - Update excludes
- `server-admin/src/main/java/com/lesofn/appforge/server/admin/config/SecurityConfig.java` - Remove druid path from security config
- `server-admin/src/main/resources/application.yaml` - Remove druid monitoring config, add dynamic-datasource base config
- `server-admin/src/main/resources/application-dev.yaml` - Replace custom JDBC properties with dynamic-datasource format
- `server-admin/src/main/resources/application-prod.yaml.example` - Same
- `domain/admin-user/src/main/java/com/lesofn/appforge/user/config/UserDataSourceConfig.java` - Simplify to use GroupDataSourceProxy
- `domain/admin-user/src/main/java/com/lesofn/appforge/user/config/UserDbConfig.java` - No changes needed (still references `userDataSource` bean)
- `example/example-task/src/main/java/com/lesofn/appforge/demo/task/config/TaskDataSourceConfig.java` - Simplify to use GroupDataSourceProxy
- `example/example-task/src/main/java/com/lesofn/appforge/demo/task/dao/TaskDbConfig.java` - No changes needed
- `infrastructure/src/main/java/com/lesofn/appforge/infrastructure/frame/context/RequestContext.java` - Remove readMasterDB/shouldReadMasterDB fields
- `CLAUDE.md` - Update docs

### Deleted Files
- `infrastructure/src/main/java/com/lesofn/appforge/infrastructure/frame/database/ReadWriteDataSourceRouter.java`
- `infrastructure/src/main/java/com/lesofn/appforge/infrastructure/frame/database/DataBaseAdvisor.java`
- `infrastructure/src/main/java/com/lesofn/appforge/infrastructure/frame/database/DataBaseType.java`

---

### Task 1: Update Dependencies

**Files:**
- Modify: `dependencies/build.gradle.kts`
- Modify: `common/common-core/build.gradle.kts`
- Modify: `server-admin/build.gradle.kts`

- [ ] **Step 1: Update dependencies/build.gradle.kts**

Replace the Druid dependencies with dynamic-datasource:

```kotlin
// OLD (lines 17-19):
api("com.alibaba:druid:1.2.25")
api("com.alibaba:druid-spring-boot-3-starter:1.2.25")

// NEW:
api("com.baomidou:dynamic-datasource-spring-boot4-starter:4.5.0")
```

Keep `com.mysql:mysql-connector-j` and `com.h2database:h2` as-is.

- [ ] **Step 2: Update common/common-core/build.gradle.kts**

Replace:
```kotlin
api("com.alibaba:druid")
```
with:
```kotlin
api("com.baomidou:dynamic-datasource-spring-boot4-starter")
```

- [ ] **Step 3: Update server-admin/build.gradle.kts**

Remove:
```kotlin
// Druid connection pool and monitoring
api("com.alibaba:druid-spring-boot-3-starter")
```

- [ ] **Step 4: Verify dependencies resolve**

Run: `export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 && cd /home/sofn/code/sofn/AppForge && ./gradlew :server-admin:dependencies --configuration compileClasspath 2>&1 | grep -E 'dynamic-datasource|druid|FAILED'`

Expected: `dynamic-datasource-spring-boot4-starter` resolved, no `druid-spring-boot-3-starter`

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "build: replace druid-spring-boot-3-starter with dynamic-datasource-spring-boot4-starter"
```

---

### Task 2: Create GroupDataSourceProxy

**Files:**
- Create: `infrastructure/src/main/java/com/lesofn/appforge/infrastructure/frame/database/GroupDataSourceProxy.java`

- [ ] **Step 1: Create GroupDataSourceProxy**

This is a lightweight `DataSource` wrapper that pushes a datasource group name onto `DynamicDataSourceContextHolder` before delegating to `DynamicRoutingDataSource`. This allows each JPA `EntityManagerFactory` to always connect to the correct database group.

```java
package com.lesofn.appforge.infrastructure.frame.database;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * DataSource proxy that routes to a specific dynamic-datasource group.
 * Pushes the group name onto DynamicDataSourceContextHolder before
 * obtaining a connection, ensuring the correct datasource is used.
 *
 * @author sofn
 */
public class GroupDataSourceProxy implements DataSource {

    private final DataSource delegate;
    private final String group;

    public GroupDataSourceProxy(DataSource delegate, String group) {
        this.delegate = delegate;
        this.group = group;
    }

    @Override
    public Connection getConnection() throws SQLException {
        DynamicDataSourceContextHolder.push(group);
        try {
            return delegate.getConnection();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        DynamicDataSourceContextHolder.push(group);
        try {
            return delegate.getConnection(username, password);
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add -A && git commit -m "feat: add GroupDataSourceProxy for dynamic-datasource multi-db support"
```

---

### Task 3: Delete Custom Read-Write Split Infrastructure

**Files:**
- Delete: `infrastructure/src/main/java/com/lesofn/appforge/infrastructure/frame/database/ReadWriteDataSourceRouter.java`
- Delete: `infrastructure/src/main/java/com/lesofn/appforge/infrastructure/frame/database/DataBaseAdvisor.java`
- Delete: `infrastructure/src/main/java/com/lesofn/appforge/infrastructure/frame/database/DataBaseType.java`
- Modify: `infrastructure/src/main/java/com/lesofn/appforge/infrastructure/frame/context/RequestContext.java`

- [ ] **Step 1: Delete the three files**

```bash
rm infrastructure/src/main/java/com/lesofn/appforge/infrastructure/frame/database/ReadWriteDataSourceRouter.java
rm infrastructure/src/main/java/com/lesofn/appforge/infrastructure/frame/database/DataBaseAdvisor.java
rm infrastructure/src/main/java/com/lesofn/appforge/infrastructure/frame/database/DataBaseType.java
```

- [ ] **Step 2: Remove readMasterDB/shouldReadMasterDB from RequestContext.java**

In `/home/sofn/code/sofn/AppForge/infrastructure/src/main/java/com/lesofn/appforge/infrastructure/frame/context/RequestContext.java`:

Remove these fields (lines 42-43):
```java
// 直接从主库中读取数据
private boolean readMasterDB;
private boolean shouldReadMasterDB;
```

Remove from constructor (line 49):
```java
readMasterDB = false;
```

Remove all getter/setter methods (lines 120-134):
```java
public boolean isReadMasterDB() { ... }
public void setReadMasterDB(boolean readMasterDB) { ... }
public boolean isShouldReadMasterDB() { ... }
public void setShouldReadMasterDB(boolean shouldReadMasterDB) { ... }
```

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "refactor: remove custom ReadWriteDataSourceRouter, DataBaseAdvisor, DataBaseType"
```

---

### Task 4: Simplify UserDataSourceConfig

**Files:**
- Modify: `domain/admin-user/src/main/java/com/lesofn/appforge/user/config/UserDataSourceConfig.java`

- [ ] **Step 1: Rewrite UserDataSourceConfig**

Replace the entire file with:

```java
package com.lesofn.appforge.user.config;

import com.lesofn.appforge.infrastructure.frame.database.GroupDataSourceProxy;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * User DataSource Configuration.
 * Uses dynamic-datasource "user" group (user_master + user_slave).
 */
@Configuration
public class UserDataSourceConfig {

    /**
     * User DataSource - proxies to the "user" dynamic-datasource group.
     * The group's master-slave routing is handled by dynamic-datasource strategy.
     */
    @Bean
    public DataSource userDataSource(DataSource dataSource) {
        return new GroupDataSourceProxy(dataSource, "user");
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add -A && git commit -m "refactor: simplify UserDataSourceConfig to use GroupDataSourceProxy"
```

---

### Task 5: Simplify TaskDataSourceConfig

**Files:**
- Modify: `example/example-task/src/main/java/com/lesofn/appforge/demo/task/config/TaskDataSourceConfig.java`

- [ ] **Step 1: Rewrite TaskDataSourceConfig**

Replace the entire file with:

```java
package com.lesofn.appforge.demo.task.config;

import com.lesofn.appforge.infrastructure.frame.database.GroupDataSourceProxy;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Task DataSource Configuration.
 * Uses dynamic-datasource "task" group (task_master + task_slave).
 */
@Configuration
public class TaskDataSourceConfig {

    /**
     * Task DataSource - proxies to the "task" dynamic-datasource group.
     * The group's master-slave routing is handled by dynamic-datasource strategy.
     */
    @Bean
    public DataSource taskDataSource(DataSource dataSource) {
        return new GroupDataSourceProxy(dataSource, "task");
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add -A && git commit -m "refactor: simplify TaskDataSourceConfig to use GroupDataSourceProxy"
```

---

### Task 6: Update Application.java

**Files:**
- Modify: `server-admin/src/main/java/com/lesofn/appforge/server/admin/Application.java`

- [ ] **Step 1: Update auto-configuration excludes**

The `DynamicDataSourceAutoConfiguration` from dynamic-datasource must NOT be excluded (it creates the `DynamicRoutingDataSource` bean). We keep excluding `DataSourceAutoConfiguration`, `HibernateJpaAutoConfiguration`, and `DataSourceTransactionManagerAutoConfiguration`.

Also add exclude for `DruidDataSourceAutoConfigure` in case the Druid dependency is pulled transitively by dynamic-datasource (this prevents the old incompatible Druid auto-config from running).

```java
package com.lesofn.appforge.server.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author sofn
 * @version 1.0 Created at: 2015-04-29 16:17
 */
@ComponentScan(basePackages = "com.lesofn.appforge")
@SpringBootApplication(
        exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class
        })
@EnableTransactionManagement
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

Note: The file content stays the same. The `DynamicDataSourceAutoConfiguration` is NOT in the exclude list (it's auto-discovered from the starter).

- [ ] **Step 2: Commit (if changed)**

---

### Task 7: Update YAML Configuration

**Files:**
- Modify: `server-admin/src/main/resources/application.yaml`
- Modify: `server-admin/src/main/resources/application-dev.yaml`
- Modify: `server-admin/src/main/resources/application-prod.yaml.example`

- [ ] **Step 1: Update application.yaml**

Remove the old Druid monitoring section and add dynamic-datasource base config:

```yaml
# Remove this entire section:
  datasource:
    druid:
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        reset-enable: false
      web-stat-filter:
        enabled: true
        url-pattern: /*
        exclusions: '*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*'

# Add dynamic-datasource base config under spring:
  datasource:
    dynamic:
      primary: user_master
      strict: false
      druid:
        initial-size: 0
        max-active: 5
        min-idle: 1
        max-wait: 60000
        time-between-eviction-runs-millis: 60000
        min-evictable-idle-time-millis: 300000
        validation-query: SELECT 1
        test-while-idle: true
        test-on-borrow: false
        test-on-return: false
        pool-prepared-statements: true
        max-pool-prepared-statement-per-connection-size: 20
        stat:
          merge-sql: true
          slow-sql-millis: 500
          log-slow-sql: true
```

- [ ] **Step 2: Update application-dev.yaml**

Replace the old custom JDBC property blocks:

```yaml
# Remove all of these:
task:
  jdbc:
    master:
      driver: org.h2.Driver
      url: ...
    slave:
      ...
user:
  jdbc:
    master:
      ...
    slave:
      ...

# Add under spring:
spring:
  datasource:
    dynamic:
      datasource:
        user_master:
          driver-class-name: org.h2.Driver
          url: jdbc:h2:file:~/.h2/user;AUTO_SERVER=TRUE;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1
          username: sa
          password: ""
        user_slave:
          driver-class-name: org.h2.Driver
          url: jdbc:h2:file:~/.h2/user;AUTO_SERVER=TRUE;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1
          username: sa
          password: ""
        task_master:
          driver-class-name: org.h2.Driver
          url: jdbc:h2:file:~/.h2/task;AUTO_SERVER=TRUE;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1
          username: sa
          password: ""
        task_slave:
          driver-class-name: org.h2.Driver
          url: jdbc:h2:file:~/.h2/task;AUTO_SERVER=TRUE;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1
          username: sa
          password: ""
```

- [ ] **Step 3: Update application-prod.yaml.example**

Same pattern - replace old custom JDBC properties:

```yaml
spring:
  datasource:
    dynamic:
      datasource:
        user_master:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://<db-master-host>:3306/user_prod?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
          username: ${DB_USERNAME}
          password: ${DB_PASSWORD}
        user_slave:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://<db-slave-host>:3306/user_prod?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
          username: ${DB_USERNAME}
          password: ${DB_PASSWORD}
        task_master:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://<db-master-host>:3306/task_prod?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
          username: ${DB_USERNAME}
          password: ${DB_PASSWORD}
        task_slave:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://<db-slave-host>:3306/task_prod?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
          username: ${DB_USERNAME}
          password: ${DB_PASSWORD}
```

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "config: migrate datasource config to dynamic-datasource YAML format"
```

---

### Task 8: Update SecurityConfig (Druid path removal)

**Files:**
- Modify: `server-admin/src/main/java/com/lesofn/appforge/server/admin/config/SecurityConfig.java`

- [ ] **Step 1: Remove Druid path from security config**

In `SecurityConfig.java`, line 180 has:
```java
.requestMatchers("/druid/**")
```

Remove this line since Druid monitoring servlet is no longer configured.

- [ ] **Step 2: Commit**

```bash
git add -A && git commit -m "fix: remove druid path from security config"
```

---

### Task 9: Fix TaskDbConfig StringUtils deprecation

**Files:**
- Modify: `example/example-task/src/main/java/com/lesofn/appforge/demo/task/dao/TaskDbConfig.java`

- [ ] **Step 1: Replace deprecated StringUtils calls**

In `TaskDbConfig.java`, line 47 uses deprecated `StringUtils.substring` and `StringUtils.lastIndexOf`:

```java
// OLD:
import org.apache.commons.lang3.StringUtils;
...
factoryBean.setPackagesToScan(
    StringUtils.substring(packageName, 0, StringUtils.lastIndexOf(packageName, '.')));

// NEW:
// Remove StringUtils import, use standard String methods:
factoryBean.setPackagesToScan(packageName.substring(0, packageName.lastIndexOf('.')));
```

- [ ] **Step 2: Commit**

```bash
git add -A && git commit -m "fix: replace deprecated StringUtils calls in TaskDbConfig"
```

---

### Task 10: Build, Spotless, and Verify

**Files:**
- Modify: `CLAUDE.md` (update docs)

- [ ] **Step 1: Run spotlessApply**

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 && cd /home/sofn/code/sofn/AppForge && ./gradlew spotlessApply
```

- [ ] **Step 2: Run full build**

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 && cd /home/sofn/code/sofn/AppForge && ./gradlew clean build
```

Expected: BUILD SUCCESSFUL with no deprecation warnings related to datasource/druid.

- [ ] **Step 3: Fix any build errors**

If there are compilation errors (e.g., remaining references to deleted classes), fix them.

- [ ] **Step 4: Update CLAUDE.md**

Update the Dependencies section to reflect the change from Druid to dynamic-datasource.

- [ ] **Step 5: Final commit and push**

```bash
git add -A && git commit -m "feat: complete migration to dynamic-datasource-spring-boot4-starter

- Replace druid-spring-boot-3-starter with dynamic-datasource-spring-boot4-starter
- Remove custom ReadWriteDataSourceRouter, DataBaseAdvisor, DataBaseType
- Add GroupDataSourceProxy for multi-database JPA support
- Migrate YAML config to dynamic-datasource format
- Remove Druid monitoring servlet config
- Simplify UserDataSourceConfig and TaskDataSourceConfig"

git push origin dev
```

---

## Notes

### Read-Write Split Strategy After Migration

The old approach used AOP to detect method name prefixes (get*, find* → slave, save*, update* → master). This is removed.

With dynamic-datasource, read-write split is available via:
1. **`@DS("user_master")` / `@DS("user_slave")`** - explicit annotation on service methods
2. **`@DSTransactional`** - forces master within transaction
3. **Group strategy** - configurable in YAML: `spring.datasource.dynamic.strategy`

For now, the default behavior routes to the **first datasource in each group** (the master). Explicit slave routing can be added later as needed.

### Druid Monitoring

Druid monitoring servlet (`/druid/*`) is removed since we no longer use `druid-spring-boot-3-starter`. Connection pool monitoring is available via:
- Spring Boot Actuator: `http://localhost:7002/metrics` (already configured)
- HikariCP metrics (if switching to HikariCP later)
- dynamic-datasource's built-in Druid stat integration via YAML config
