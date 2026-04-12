AppBoot 是一个分布式的App服务端快速开发框架,包含了基本的权限认证、日志处理、接口防刷、系统监控等基本功能。
此框架围绕分布式服务系统构建，能够快速扩容，迎合微服务化，提供App服务端常用必备功能。

- **认证方式** - 基于 JWT 的认证，支持 Basic、Cookie、Header、内外网
- **统一错误处理** - 集中式错误码和统一 JSON 响应格式
- **请求日志** - 通过 AOP 自动记录请求/响应日志
- **接口限流** - 内置 API 频次拦截器
- **多数据源** - 支持主从读写分离
- **多环境配置** - Gradle、Spring、应用三层 Profile 整合（`dev` / `test` / `prod`）
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

在 `gradle.properties` 中设置：
```properties
profile=dev
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
