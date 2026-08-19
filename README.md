<div align="center">
  <h1>ArchForge</h1>
  <p><strong>Backend for the ArchForge platform — Spring Boot 4 + JDK 25 + sa-token</strong></p>
  <p>
    <a href="https://archforge.lesofn.com">Documentation</a> ·
    <a href="./README.zh-CN.md">中文</a>
  </p>
  <p>
    <img src="https://img.shields.io/badge/Java-25-blue?logo=openjdk" alt="Java 25" />
    <img src="https://img.shields.io/badge/Spring%20Boot-4.1.0-green?logo=springboot" alt="Spring Boot 4.1" />
    <img src="https://img.shields.io/badge/Gradle-9.5.1-02303A?logo=gradle" alt="Gradle 9.5.1" />
    <img src="https://img.shields.io/badge/Auth-sa--token%201.45-red" alt="sa-token" />
    <img src="https://img.shields.io/badge/License-MIT-yellow" alt="MIT" />
  </p>
</div>

---

## What is this repo?

This repository is the **backend** of the five-repo ArchForge project. It exposes two Spring Boot apps:

| App | Gradle module | Port | Audience |
|-----|---------------|------|----------|
| Admin API | `:archforge-server-admin` | 8080 | ArchForgeAdmin (Vue) |
| C-end API | `:archforge-server-web` | 8081 | ArchForgeWeb (Next.js) |

Sibling repositories (clone side by side, no submodules):

```
archforge/
├── ArchForge/          # this repo
├── ArchForgeWeb/       # C-end Next.js client
├── ArchForgeAdmin/     # admin Vue client
├── ArchForgeDocs/      # VitePress docs
└── ArchForgeSpec/      # contracts, enums, architecture
```

Contracts live in `../ArchForgeSpec`. When an API changes, update `../ArchForgeSpec/api/openapi.yaml` in the same change.

## Auth and security

- **sa-token 1.45.0**, not Spring Security JWT filters.
- Admin: `StpAdminUtil` + class-level `@SaCheckLogin` + write `@SaCheckPermission("resource:action")`.
- Web: `StpWebUtil` + `WebAuthInterceptor`.
- Login rate limit: 5 requests / minute / IP (`@RateLimit`).
- XSS filter sanitizes query/header values and **skips multipart** uploads.
- Production YAML has **no default** `DB_PASSWORD` / S3 keys.

Admin responses use `{code,message,data}`. Web errors use RFC 9457 `ProblemDetail`.

Admin extras:

- Dashboard: `GET /admin/dashboard/metrics|trends|recent-activities|todo`
- Permission matrix: `/admin/permission-matrix/**`
- ChatAI: `/admin/chat/**` with user-supplied `LLM_PROVIDER` / `LLM_BASE_URL` / `LLM_API_KEY` / `LLM_MODEL` (OpenAI or Anthropic compatible). No default key.

## Gradle modules

Every Java module is prefixed with `archforge-`:

```
archforge-common/archforge-common-{base,error,jpa}
archforge-domain/archforge-{admin-user,blog,meta-table}
archforge-infrastructure
archforge-server-admin
archforge-server-web
archforge-cli
archforge-example/archforge-example-task   # still linked from server-admin (/task)
archforge-starters/archforge-{cache,lock,redisson,trace}-starter
archforge-dependencies
```

`archforge-server-admin` still depends on `archforge-example-task` because the admin `/task` API uses it. AuthSpi stays — `DefaultAuthService` and `ProxyResource` still call it.

Non-Gradle dirs stay unprefixed: `docker/`, `config/`, `scripts/`, `skills/`.

## Developer CLI

```bash
./archforge --help
./archforge init --write          # idempotent .env secrets
./archforge infra up              # postgres + redis via docker/docker-compose.infra.yml
./archforge db backup
./archforge skills install --tool claude
./archforge --mcp                 # Phase-1 MCP stdio server
```

If the fat jar is missing, `./archforge` builds `:archforge-cli:shadowJar` first.

## Quick start

Prerequisites: **Java 25**, Docker.

```bash
git clone <archforge> ArchForge
cd ArchForge
./archforge init --write
./archforge infra up
# source .env so DB_PASSWORD and sa-token secrets are present
set -a; source .env; set +a
FILE_STORAGE_TYPE=local ./gradlew :archforge-server-admin:bootRun
```

Default admin login is `admin / admin123` (captcha is on in `dev`).

Web API:

```bash
./gradlew :archforge-server-web:bootRun
```

## Build and test

```bash
./gradlew build                                   # spotless + compile + all tests
./gradlew :archforge-server-admin:test
./gradlew :archforge-cli:test
```

## Tech stack

| Layer | Choice |
|-------|--------|
| Runtime | Java 25 (preview), Spring Boot **4.1.0**, Gradle **9.5.1** |
| Auth | sa-token **1.45.0** (`StpAdminUtil` / `StpWebUtil`) |
| Data | PostgreSQL 17, Flyway **12.4.0**, dynamic-datasource, Redis 7 |
| Observability | Micrometer + OpenTelemetry **1.62.0** |
| Storage | Local dir or AWS S3 SDK 2.46.x |
| Style | Spotless + Google Java Style, JSpecify `@NullMarked` |

Canonical conventions: `skills/archforge-project-standard/standard.md`.

## License

[MIT](./LICENSE)
