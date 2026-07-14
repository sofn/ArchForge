<div align="center">
  <h1>ArchForge</h1>
  <p><strong>Modern enterprise admin platform built with Spring Boot 4 + Vue 3</strong></p>
  <p>
    <a href="https://archforge.lesofn.com">Documentation</a> ·
    <a href="https://github.com/sofn/ArchForgeAdmin">Frontend Repo</a> ·
    <a href="./README.zh-CN.md">中文</a>
  </p>
  <p>
    <img src="https://img.shields.io/badge/Java-25-blue?logo=openjdk" alt="Java 25" />
    <img src="https://img.shields.io/badge/Spring%20Boot-4.0.5-green?logo=springboot" alt="Spring Boot 4" />
    <img src="https://img.shields.io/badge/Vue-3.5-brightgreen?logo=vuedotjs" alt="Vue 3" />
    <img src="https://img.shields.io/badge/Vite-8-purple?logo=vite" alt="Vite 8" />
    <img src="https://img.shields.io/badge/License-MIT-yellow" alt="MIT" />
  </p>
</div>

---

## What is ArchForge?

ArchForge is a **production-ready, full-stack admin platform** that combines a Spring Boot 4 backend with a Vue 3 frontend. It provides complete user/role/menu/department management, file management, Quartz scheduling, i18n, operation/login logs, server monitoring, JWT authentication, and more — all with clean architecture and modern tooling.

### Why ArchForge?

- **Modern architecture**: JDK 25 + Spring Boot 4 + DDD + Clean Architecture — not a legacy codebase ported forward
- **Team project standard**: Codified conventions (Spotless, JSpecify, Lombok), centralized dependency BOM, skill-based onboarding
- **JDK 25 features in production**: ScopedValue, Structured Concurrency, Pattern Matching, Stream Gatherers, Virtual Threads
- **Production-ready deployment**: Docker with jlink minimal JRE + Project Leyden CDS, Flyway migrations, multi-datasource, Micrometer observability
- **Zero-config dev**: `scripts/dev/init.sh` starts PostgreSQL, Redis, RustFS Docker containers; `./gradlew server-admin:bootRun` connects to them

## Features

| Category | Details |
|----------|---------|
| **Auth** | JWT + refresh token, Spring Security, BCrypt password, configurable captcha |
| **RBAC** | Users, roles, menus, departments, button-level permissions |
| **System** | Config management, notice/announcements, operation & login logs |
| **File Management** | Upload, list, download, delete; local filesystem and S3 (RustFS) backends; extension/size/MIME allow-lists |
| **Scheduler** | Quartz-based reflective cron jobs; pause/resume/run once; execution logs |
| **i18n** | Backend and frontend locale sync; English / Simplified Chinese message bundles |
| **Monitor** | Real-time CPU/memory/JVM/disk monitoring (Oshi), Druid SQL monitoring, embedded Swagger UI |
| **Database** | PostgreSQL, multi-datasource with read/write split, Flyway migration |
| **Deploy** | Docker Compose (Leyden JVM + Native Image), Nginx reverse proxy |
| **Frontend** | vue-pure-admin, Element Plus, TailwindCSS, Pinia, dynamic routing, i18n |
| **Architecture** | Spring Modulith 2.0 module boundaries, typed DTO APIs, JSpecify @NullMarked, Spotless |

## Recent Highlights

The latest merged feature branch added a number of production-ready capabilities:

- **File Management** — A complete file lifecycle (upload, list, download, delete) with configurable local or S3/RustFS storage, extension allow-lists and size limits.
- **Quartz Scheduling** — Reflective cron jobs managed from the admin UI, including pause/resume/run-once and execution logs.
- **i18n** — Locale-aware message bundles on both backend and frontend, with English and Simplified Chinese out of the box.
- **Druid Monitoring** — Druid SQL monitoring enabled in non-production environments and hidden in production.
- **Spring Modulith 2.0** — Explicit module boundaries and dependency tests for the `admin-user`, `example-task`, and infrastructure layers.
- **Security Hardening** — Management endpoints require admin/authenticated access, legacy endpoints removed, and sensitive data no longer logged.
- **Typed API Contracts** — Controllers moved from raw `Map` payloads to explicit request/response DTOs with MapStruct mapping.
- **Quality of Life** — `@RepeatSubmit` anti-replay, `LIKE` escape compatibility for Druid `mergeSql`, and JDK 25 native access enabled for all test/AOT tasks.

## Quick Start

### Prerequisites

- Java 25, Node.js 20+, pnpm 9+
- **Docker** (required for dev mode — Testcontainers uses Docker to run PostgreSQL, Redis, RustFS)

### 1. Clone

```bash
git clone https://github.com/sofn/ArchForge.git
git clone https://github.com/sofn/ArchForgeAdmin.git
```

### 2. Start Dev Environment

```bash
cd ArchForge/scripts/dev
./init.sh           # starts PostgreSQL, Redis, RustFS Docker containers
cd ../..
JAVA_HOME=/path/to/jdk25 ./gradlew server-admin:bootRun
```

> Dev profile uses the Docker containers started by `scripts/dev/init.sh`. Use `scripts/dev/down.sh` to stop them.

### 3. Start Frontend

```bash
cd ArchForgeAdmin
pnpm install && pnpm dev
```

### 4. Open Browser

Visit `http://localhost:8848` and login with `admin / admin123`.

### Staging / Production Deployment

```bash
cd ArchForge/scripts/staging  # or scripts/prod
cp .env.example .env          # edit configuration
./deploy.sh
```

This builds both backend and frontend Docker images, starts PostgreSQL + Redis + backend + frontend via Docker Compose, and imports seed SQL.

### Docker (Alternative)

```bash
cd ArchForge/docker
./start.sh          # JVM mode (default, Project Leyden CDS)
./start.sh native   # Native Image mode (Liberica NIK 25)
```

## Project Structure

```
ArchForge (Backend)
├── common/                   # Shared libraries
│   ├── common-base/          # Core utilities, enums, Jackson, encryption, validation
│   ├── common-error/         # Error codes and business exceptions
│   └── common-jpa/           # JPA base entities, converters, and QueryHelp
├── infrastructure/           # Auth, filters, file storage, i18n, response wrapper
├── domain/admin-user/        # Domain entities & business logic
│   ├── domain/               # SysUser, SysRole, SysMenu, SysDept, SysFile, SysQuartzJob, SysQuartzLog
│   ├── service/              # Business services
│   └── dao/                  # Spring Data JPA repositories
├── server-admin/             # Web layer & Spring Boot app
├── example/example-task/     # Task domain example
├── dependencies/             # Centralized version management
└── docker/                   # Docker & deployment configs
    ├── jvm/                  # Leyden CDS optimized Dockerfile
    └── native/               # Liberica NIK 25 native Dockerfile

ArchForgeAdmin (Frontend)
├── src/api/                  # API definitions
├── src/views/system/         # System management pages
├── src/views/system/quartz/  # Quartz job scheduling pages
├── src/views/monitor/        # Monitoring pages (server, cache, logs)
├── src/views/tool/           # File management page
├── src/store/                # Pinia stores
└── src/router/               # Dynamic routing
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 25, Spring Boot 4.0.5, Spring Security, Spring Data JPA, QueryDSL, Spring Modulith 2.0 |
| Frontend | Vue 3.5, Vite 8, TypeScript 6, Element Plus, TailwindCSS 4 |
| Database | PostgreSQL 17 (Testcontainers in dev), Redis, Flyway |
| File Storage | Local filesystem, AWS S3 / RustFS (Testcontainers in dev) |
| Monitoring | Oshi, SpringDoc OpenAPI, Micrometer + OpenTelemetry |
| Build | Gradle 9.4.1, pnpm, Docker, Project Leyden, Liberica NIK 25 |
| Testing | JUnit 6, Spock 2.4, RestClient, Testcontainers |

## Documentation

Full documentation: **[archforge.lesofn.com](https://archforge.lesofn.com)**

## License

[MIT](./LICENSE)
