<div align="center">
  <h1>ArchForge</h1>
  <p><strong>ArchForge 后端 — Spring Boot 4 + JDK 25 + sa-token</strong></p>
  <p>
    <a href="https://archforge.lesofn.com">在线文档</a> ·
    <a href="./README.md">English</a>
  </p>
</div>

## 这是哪个仓库？

本仓库是五仓 ArchForge 的 **后端**。提供两个 Spring Boot 应用：

| 应用 | Gradle 模块 | 端口 | 消费方 |
|------|-------------|------|--------|
| 管理端 API | `:archforge-server-admin` | 8080 | ArchForgeAdmin |
| C 端 API | `:archforge-server-web` | 8081 | ArchForgeWeb |

兄弟仓并排克隆、无 submodule：`ArchForge` / `ArchForgeWeb` / `ArchForgeAdmin` / `ArchForgeDocs` / `ArchForgeSpec`。

契约在 `../ArchForgeSpec`。API 变更必须同步 `api/openapi.yaml`。

## 认证与安全

- **sa-token 1.45.0**，不是 Spring Security JWT Filter。
- 管理端：`StpAdminUtil` + 类级 `@SaCheckLogin` + 写操作 `@SaCheckPermission`。
- C 端：`StpWebUtil` + `WebAuthInterceptor`。
- 登录限流 5 次/分钟/IP；XSS 过滤 query/header，**跳过 multipart**。
- 生产配置无默认数据库/S3 密码。

管理端响应 `{code,message,data}`；C 端错误为 RFC 9457 ProblemDetail。

附加接口：

- 仪表盘 `GET /admin/dashboard/metrics|trends|recent-activities|todo`
- 权限矩阵 `/admin/permission-matrix/**`
- ChatAI `/admin/chat/**`，密钥由用户填写 `LLM_PROVIDER` / `LLM_BASE_URL` / `LLM_API_KEY` / `LLM_MODEL`（OpenAI 或 Anthropic 兼容），仓库不内置密钥

## 模块与 CLI

所有 Java 模块带 `archforge-` 前缀。`archforge-server-admin` 仍依赖 `archforge-example-task`（`/task` API）。AuthSpi 因 `DefaultAuthService` / `ProxyResource` 仍在使用，未删除。

开发入口：

```bash
./archforge --help
./archforge init --write
./archforge infra up
FILE_STORAGE_TYPE=local ./gradlew :archforge-server-admin:bootRun
```

## 构建

```bash
./gradlew build
```

需要 **Java 25**。约定见 `skills/archforge-project-standard/standard.md`。
