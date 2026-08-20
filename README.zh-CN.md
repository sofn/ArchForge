<div align="center">
  <h1>ArchForge</h1>
  <p><strong>ArchForge 后端 — Spring Boot 4 + JDK 25 + sa-token</strong></p>
  <p>
    <a href="https://archforge.lesofn.com">在线文档</a> ·
    <a href="./README.md">English</a>
  </p>
  <p>
    <img src="https://img.shields.io/badge/Java-25-blue?logo=openjdk" alt="Java 25" />
    <img src="https://img.shields.io/badge/Spring%20Boot-4.1.0-green?logo=springboot" alt="Spring Boot 4.1" />
    <img src="https://github.com/sofn/ArchForge/actions/workflows/ci.yml/badge.svg" alt="CI" />
    <img src="https://img.shields.io/badge/Auth-sa--token%201.45-red" alt="sa-token" />
    <img src="https://img.shields.io/badge/License-Apache%202.0-blue" alt="Apache 2.0" />
  </p>
</div>

---

## 这是哪个仓库？

本仓库是五仓 ArchForge 的 **后端**。提供两个 Spring Boot 应用：

| 应用 | Gradle 模块 | 端口 | 消费方 |
|------|-------------|------|--------|
| 管理端 API | `:archforge-server-admin` | 8080 | ArchForgeAdmin |
| C 端 API | `:archforge-server-web` | 8081 | ArchForgeWeb |

并列克隆、无 submodule：

```
archforge/
├── ArchForge/          # 本仓库
├── ArchForgeWeb/       # C 端 Next.js
├── ArchForgeAdmin/     # 管理端 Vue
├── ArchForgeDocs/      # VitePress 文档
└── ArchForgeSpec/      # 契约、枚举、架构
```

契约在 `../ArchForgeSpec`。API 变更必须同步 `../ArchForgeSpec/api/openapi.yaml`。

## 认证与安全

- **sa-token 1.45.0**，不是 Spring Security JWT Filter。
- 管理端：`StpAdminUtil` + 类级 `@SaCheckLogin` + 写操作 `@SaCheckPermission("resource:action")`。
- C 端：`StpWebUtil` + `WebAuthInterceptor`。
- 登录限流：5 次/分钟/IP（`@RateLimit`）。
- XSS 过滤 query/header，**跳过 multipart**。C 端上传走共享 `FileUploadValidator`，拒绝 SVG/HTML。
- 生产 YAML **没有默认** `DB_PASSWORD` / S3 密钥。

管理端成功响应 `{code,message,data}`。C 端错误为 RFC 9457 `ProblemDetail`。

附加能力：

- 仪表盘 `GET /admin/dashboard/metrics|trends|recent-activities|todo`
- 权限矩阵 `/admin/permission-matrix/**`
- ChatAI `/admin/chat/**`，密钥由环境变量 `LLM_PROVIDER` / `LLM_BASE_URL` / `LLM_API_KEY` / `LLM_MODEL` 提供（OpenAI 或 Anthropic 兼容），仓库不内置密钥

## Gradle 模块

所有 Java 模块带 `archforge-` 前缀：

```
archforge-common/archforge-common-{base,error,jpa}
archforge-domain/archforge-{admin-user,blog,meta-table}
archforge-infrastructure
archforge-server-admin
archforge-server-web
archforge-cli
archforge-example/archforge-example-task   # 仍被 server-admin /task 使用
archforge-starters/archforge-{cache,lock,redisson,trace}-starter
archforge-dependencies
```

`archforge-server-admin` 仍依赖 `archforge-example-task`（`/task` API）。AuthSpi 因 `DefaultAuthService` / `ProxyResource` 仍在使用，未删除。

非 Gradle 目录不加前缀：`docker/`、`config/`、`scripts/`、`skills/`。

## 开发者 CLI

```bash
./archforge --help
./archforge init --write          # 幂等写入 .env 密钥
./archforge infra up              # postgres + redis
./archforge db backup
./archforge skills install --tool claude
./archforge --mcp                 # Phase-1 MCP stdio
```

缺少 fat jar 时，`./archforge` 会先构建 `:archforge-cli:shadowJar`。

## 快速开始

需要 **Java 25** 和 Docker。

```bash
git clone <archforge> ArchForge
cd ArchForge
./archforge init --write
./archforge infra up
set -a; source .env; set +a
FILE_STORAGE_TYPE=local ./gradlew :archforge-server-admin:bootRun
```

默认管理员 `admin / admin123`（`dev` 开验证码）。

C 端 API：

```bash
./gradlew :archforge-server-web:bootRun
```

## 构建与测试

```bash
./gradlew build                                   # spotless + 编译 + 全部测试
./gradlew :archforge-server-admin:test
./gradlew :archforge-cli:test
```

约定见 `skills/archforge-project-standard/standard.md`。每个 `src/main/java` 包必须有 `@NullMarked` 的 `package-info.java`。

## License

[Apache-2.0](./LICENSE)
