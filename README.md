# AppForge

<p>
  <a href="#english">English</a> | <a href="#中文">中文</a>
</p>

---

<a id="english"></a>

## English

**AppForge** is a production-ready, modular Spring Boot application scaffold for rapidly building backend services. It provides authentication, logging, rate limiting, monitoring, and multi-datasource support out of the box.

### Tech Stack

| Category | Version |
|----------|---------|
| Java | 21 |
| Spring Boot | 4.0.5 |
| Gradle | 9.4.1 |
| Hibernate / JPA | 7.x |
| Testing | JUnit 6, Spock 2.3 |

### Features

- **Authentication** - JWT-based auth with Basic, Cookie, Header, and internal/external network support
- **Unified Error Handling** - Centralized error codes and consistent JSON response format
- **Request Logging** - Automatic request/response logging via AOP
- **Rate Limiting** - Built-in API rate limiting interceptor
- **Multi-Datasource** - Master/slave read-write splitting support
- **Multi-Profile** - Standard Spring Boot `application-{profile}.yaml` convention (`dev` / `test` / `prod`)
- **Monitoring** - Health checks, metrics, and Druid connection pool monitoring
- **Hot Reload** - Spring Boot DevTools for rapid development
- **API Documentation** - Auto-generated via SpringDoc OpenAPI (Swagger UI)

### Project Structure

```
AppForge/
├── common/                  # Shared modules
│   ├── common-core          # Core utilities, Jackson, encryption, etc.
│   └── common-error         # Unified error handling and response formats
├── domain/                  # Domain business logic
│   └── admin-user           # User management domain
├── infrastructure/          # Cross-cutting concerns (auth, logging, AOP)
├── server-admin/            # Web layer & main application entry point
├── dependencies/            # Centralized dependency version management (BOM)
└── example/                 # Example implementations
    └── example-task
```

### Quick Start

```bash
# Build
./gradlew clean build

# Run
./gradlew server-admin:bootRun
```

The application starts on port **8080** (management port **7002**).

### Profiles

| Profile | Database | Redis | Usage |
|---------|----------|-------|-------|
| `dev`   | H2 (in-memory) | jedis-mock | Local development |
| `test`  | MySQL | Redis | Testing |
| `prod`  | MySQL | Redis | Production |

Set the active profile via environment variable or JVM argument:
```bash
# Environment variable
export SPRING_PROFILES_ACTIVE=dev

# Or JVM argument
java -Dspring.profiles.active=prod -jar server-admin/build/libs/admin-*.jar
```

For test/prod, copy the example templates and fill in real values:
```bash
cp server-admin/src/main/resources/application-test.yaml.example server-admin/src/main/resources/application-test.yaml
cp server-admin/src/main/resources/application-prod.yaml.example server-admin/src/main/resources/application-prod.yaml
```

### Monitoring Endpoints

| Endpoint | URL |
|----------|-----|
| Health Check | `http://localhost:7002/health` |
| Metrics | `http://localhost:7002/metrics` |
| Druid Monitor | `http://localhost:8080/druid` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |

### Building Executable JAR

```bash
./gradlew clean build
java -jar server-admin/build/libs/admin-*.jar
```

---

<a id="中文"></a>

## 中文

**AppForge** 是一个开箱即用的模块化 Spring Boot 应用脚手架，用于快速构建后端服务。内置认证、日志、限流、监控、多数据源等常用功能。

### 技术栈

| 类别 | 版本 |
|------|------|
| Java | 21 |
| Spring Boot | 4.0.5 |
| Gradle | 9.4.1 |
| Hibernate / JPA | 7.x |
| 测试 | JUnit 6, Spock 2.3 |

### 功能列表

- **认证方式** - 基于 JWT 的认证，支持 Basic、Cookie、Header、内外网
- **统一错误处理** - 集中式错误码和统一 JSON 响应格式
- **请求日志** - 通过 AOP 自动记录请求/响应日志
- **接口限流** - 内置 API 频次拦截器
- **多数据源** - 支持主从读写分离
- **多环境配置** - 标准 Spring Boot `application-{profile}.yaml` 约定（`dev` / `test` / `prod`）
- **系统监控** - 健康检查、性能指标、Druid 连接池监控
- **热部署** - Spring Boot DevTools 快速开发
- **接口文档** - SpringDoc OpenAPI 自动生成（Swagger UI）

### 项目结构

```
AppForge/
├── common/                  # 公共模块
│   ├── common-core          # 核心工具类：Jackson、加密、IP 解析等
│   └── common-error         # 统一错误处理和响应格式
├── domain/                  # 领域业务逻辑
│   └── admin-user           # 用户管理领域
├── infrastructure/          # 横切关注点（认证、日志、AOP）
├── server-admin/            # Web 层 & 应用入口
├── dependencies/            # 集中式依赖版本管理（BOM）
└── example/                 # 示例实现
    └── example-task
```

### 快速开始

```bash
# 构建
./gradlew clean build

# 运行
./gradlew server-admin:bootRun
```

应用默认端口 **8080**，管理端口 **7002**。

### 环境配置

| 环境 | 数据库 | Redis | 用途 |
|------|--------|-------|------|
| `dev`   | H2（内存） | jedis-mock | 本地开发 |
| `test`  | MySQL | Redis | 测试环境 |
| `prod`  | MySQL | Redis | 生产环境 |

通过环境变量或 JVM 参数设置：
```bash
# 环境变量
export SPRING_PROFILES_ACTIVE=dev

# 或 JVM 参数
java -Dspring.profiles.active=prod -jar server-admin/build/libs/admin-*.jar
```

测试/生产环境需要复制示例模板并填写真实配置：
```bash
cp server-admin/src/main/resources/application-test.yaml.example server-admin/src/main/resources/application-test.yaml
cp server-admin/src/main/resources/application-prod.yaml.example server-admin/src/main/resources/application-prod.yaml
```

### 监控端点

| 端点 | 地址 |
|------|------|
| 健康检查 | `http://localhost:7002/health` |
| 性能指标 | `http://localhost:7002/metrics` |
| Druid 监控 | `http://localhost:8080/druid` |
| Swagger 文档 | `http://localhost:8080/swagger-ui/index.html` |

### 打包运行

```bash
./gradlew clean build
java -jar server-admin/build/libs/admin-*.jar
```
