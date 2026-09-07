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
    <img src="https://github.com/sofn/ArchForge/actions/workflows/ci.yml/badge.svg" alt="CI" />
    <img src="https://img.shields.io/badge/Auth-sa--token%201.45-red" alt="sa-token" />
    <img src="https://img.shields.io/badge/License-Apache%202.0-blue" alt="Apache 2.0" />
  </p>
</div>

---

## What is this repo?

This repository is the **backend** of the five-repo ArchForge project. It builds **two Spring Boot applications from one codebase**: the admin (`:8080`) API consumed by [ArchForgeAdmin](https://github.com/sofn/ArchForgeAdmin), and the C-end (`:8081`) API consumed by [ArchForgeWeb](https://github.com/sofn/ArchForgeWeb).

## ✨ Features

- **Two apps, one codebase** — `server-admin` (B-end, `{code,message,data}` envelope) and `server-web` (C-end, RFC 9457 ProblemDetail), each with its own sa-token realm
- **Contract-first** — [ArchForgeSpec](https://github.com/sofn/ArchForgeSpec) owns OpenAPI 3.1 + enums; CI diffs the live spec against it and blocks breaking changes
- **Java 25 + virtual threads**, optional GraalVM native image (~100 ms start)
- **DDD modules** guarded by Spring Modulith verification and fail-fast ArchUnit rules
- **Meta-table engine** — low-code tables with schema evolution, code generation, and REFERENCE fields
- **ChatAI module** — bring your own LLM (OpenAI/Anthropic compatible), streamed over SSE
- **Permission matrix, data scope, db-scheduler clustering, operation/login logs** out of the box
- **Developer CLI** — `./archforge` bootstraps env secrets, docker infra, DB backups, AI skills, MCP

## 🏛 Architecture

System context — how the five sibling repositories fit together:

```mermaid
flowchart LR
  subgraph clients["Frontends"]
    A["ArchForgeAdmin<br/>Vue 3 · :8848"]
    W["ArchForgeWeb<br/>Next.js · :3000"]
  end
  subgraph backend["this repo — two Spring Boot apps"]
    SA["server-admin :8080<br/>B-end API"]
    SW["server-web :8081<br/>C-end API"]
  end
  PG[("PostgreSQL 17")]
  RD[("Redis 7")]
  SPEC["ArchForgeSpec<br/>OpenAPI · enums · rules"]

  A -->|"REST /api"| SA
  W -->|"REST + SSE"| SW
  SA --> PG & RD
  SW --> PG & RD
  SPEC -. "openapi.yaml (breaking-change CI gate)" .-> SA
  SPEC -. "generated TS types" .-> A
  SPEC -. "generated TS types" .-> W
```

Backend module layering — every Java module is prefixed with `archforge-`:

```mermaid
flowchart TD
  subgraph apps["Applications"]
    ADMIN["server-admin :8080"]
    WEB["server-web :8081"]
    CLI["cli · ./archforge + MCP"]
  end
  subgraph domain["Domain modules (bounded contexts)"]
    USER["domain/admin-user<br/>users · roles · menus · dicts"]
    BLOG["domain/blog<br/>articles · categories"]
    META["domain/meta-table<br/>low-code tables"]
  end
  INFRA["infrastructure<br/>sa-token · redis · datasources · security"]
  subgraph common["Common kernels"]
    BASE["common-base"]
    ERR["common-error"]
    JPA["common-jpa"]
  end
  STARTERS["starters<br/>cache · lock · redisson · trace"]

  ADMIN --> domain & INFRA & STARTERS
  WEB --> domain & INFRA & STARTERS
  USER --> JPA & INFRA
  BLOG --> JPA
  META --> JPA
  JPA --> BASE & ERR
```

Layering is enforced by tests: controllers never touch repositories, domain modules never depend on server packages, and every rule fails if it accidentally matches zero classes. See [`ArchitectureTest`](archforge-server-admin/src/test/java/com/lesofn/archforge/server/admin/architecture/ArchitectureTest.java).

The full story lives in the [architecture guide](https://archforge.lesofn.com/guide/architecture).

## 🚀 Quick start

Prerequisites: **Java 25**, Docker.

```bash
git clone git@github.com:sofn/ArchForge.git && cd ArchForge
./archforge init --write            # idempotent .env secrets
./archforge infra up                # postgres + redis via docker compose
set -a; source .env; set +a         # export DB_PASSWORD, sa-token secrets
FILE_STORAGE_TYPE=local ./gradlew :archforge-server-admin:bootRun
```

Default admin login is `admin / admin123` (captcha is on in `dev`). C-end API:

```bash
./gradlew :archforge-server-web:bootRun
```

## 🔐 Auth and security

- **sa-token 1.45.0**, not Spring Security JWT filters.
- Admin: `StpAdminUtil` + class-level `@SaCheckLogin` + write `@SaCheckPermission("resource:action")`.
- Web: `StpWebUtil` + `WebAuthInterceptor`, refresh-token flow with single-use rotation.
- Login rate limit: 5 requests / minute / IP (`@RateLimit`).
- XSS filter sanitizes query/header values and **skips multipart** uploads.
- Production must inject `DB_PASSWORD`, `ARCH_FORGE_RSA_PRIVATE_KEY`, and storage keys; missing RSA in `prod` fails fast.

## 📦 Modules

| Module | Purpose |
|--------|---------|
| `archforge-common/archforge-common-{base,error,jpa}` | shared kernels: utils, `ErrorCode` framework, JPA conventions |
| `archforge-domain/archforge-admin-user` | users, roles, menus, depts, dicts (DDD) |
| `archforge-domain/archforge-blog` | articles, categories |
| `archforge-domain/archforge-meta-table` | low-code table engine |
| `archforge-infrastructure` | sa-token config, Redis, dynamic datasources, security filters |
| `archforge-server-admin` | B-end application (`:8080`) |
| `archforge-server-web` | C-end application (`:8081`) |
| `archforge-starters/*` | cache / lock / redisson / trace starters |
| `archforge-cli` | `./archforge` developer CLI + MCP server |
| `archforge-example/archforge-example-task` | demo task module (still linked from `/task`) |
| `archforge-dependencies` | version platform (BOM) |

Non-Gradle dirs stay unprefixed: `docker/`, `config/`, `scripts/`, `skills/`.

## 🧪 Build, test, quality gates

```bash
./gradlew build                                    # spotless + compile + all tests
./gradlew test -Ptags=P0,contract                  # only tagged tests
./gradlew build -PexcludeTags=slow                 # skip container-backed integration tests
./gradlew jacocoAggregateReport                    # merged coverage report
```

What CI enforces on every PR ([ci.yml](.github/workflows/ci.yml)):

| Gate | Mechanism |
|------|-----------|
| Diff coverage ≥ 60% | `diff-cover` on the aggregated JaCoCo report |
| No breaking API changes | `oasdiff` live spec vs `ArchForgeSpec/api/openapi.yaml` |
| Error codes documented | `scripts/check-error-codes.py` vs `specs/error-codes.md` |
| Frontend SDK in sync | `git diff --exit-code` after regenerating `schema.d.ts` (in Web/Admin repos) |
| Architecture rules | ArchUnit with `failOnEmptyShould=true` |

## 🛠 Developer CLI

```bash
./archforge --help
./archforge init --write          # idempotent .env secrets
./archforge infra up              # postgres + redis via docker/docker-compose.infra.yml
./archforge db backup
./archforge skills install --tool claude
./archforge --mcp                 # Phase-1 MCP stdio server
```

If the fat jar is missing, `./archforge` builds `:archforge-cli:shadowJar` first.

Windows: run `archforge.bat` instead — same commands (`archforge --help`, `archforge init --write`, …). It builds the jar via `gradlew.bat` on first run and needs `java` on `PATH`.

## 📚 Tech stack

| Layer | Choice |
|-------|--------|
| Runtime | Java 25 (preview), Spring Boot **4.1.0**, Gradle **9.5.1** |
| Auth | sa-token **1.45.0** (`StpAdminUtil` / `StpWebUtil`) |
| Data | PostgreSQL 17, Flyway **12.4.0**, dynamic-datasource, Redis 7 |
| Observability | Micrometer + OpenTelemetry **1.62.0** |
| Storage | Local dir or AWS S3 SDK 2.46.x |
| Style | Spotless + Eclipse formatter, JSpecify `@NullMarked` |

Canonical conventions: `skills/archforge-project-standard/standard.md`.

## License

[Apache-2.0](./LICENSE)
