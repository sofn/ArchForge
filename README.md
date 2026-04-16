<div align="center">
  <h1>ArchSmith</h1>
  <p><strong>Modern enterprise admin platform built with Spring Boot 4 + Vue 3</strong></p>
  <p>
    <a href="https://archsmith.lesofn.com">Documentation</a> ·
    <a href="https://github.com/sofn/ArchSmithAdmin">Frontend Repo</a> ·
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

## What is ArchSmith?

ArchSmith is a **production-ready, full-stack admin platform** that combines a Spring Boot 4 backend with a Vue 3 frontend. It provides complete user/role/menu/department management, file upload/download, server monitoring, JWT authentication, and more — all with clean architecture and modern tooling.

### Why ArchSmith?

- **Modern architecture**: JDK 25 + Spring Boot 4 + DDD + Clean Architecture — not a legacy codebase ported forward
- **Team project standard**: Codified conventions (Spotless, JSpecify, Lombok), centralized dependency BOM, skill-based onboarding
- **JDK 25 features in production**: ScopedValue, Structured Concurrency, Pattern Matching, Stream Gatherers, Virtual Threads
- **Production-ready deployment**: Docker with jlink minimal JRE + Project Leyden CDS, Flyway migrations, multi-datasource, Micrometer observability
- **Zero-config dev**: `./gradlew server-admin:bootRun` auto-starts PostgreSQL, Redis, RustFS via Testcontainers

## Features

| Category | Details |
|----------|---------|
| **Auth** | JWT + refresh token, Spring Security, BCrypt password, configurable captcha |
| **RBAC** | Users, roles, menus, departments, button-level permissions |
| **System** | Config management, notice/announcements, operation & login logs |
| **File Storage** | Upload/download with local filesystem and S3 (RustFS) backends, configurable via YAML |
| **Monitor** | Real-time CPU/memory/JVM/disk monitoring (Oshi), embedded Swagger UI |
| **Database** | PostgreSQL, multi-datasource with read/write split, Flyway migration |
| **Deploy** | Docker Compose (Leyden JVM + Native Image), Nginx reverse proxy |
| **Frontend** | vue-pure-admin, Element Plus, TailwindCSS, Pinia, dynamic routing |

## Quick Start

### Prerequisites

- Java 25, Node.js 20+, pnpm 9+
- **Docker** (required for dev mode — Testcontainers uses Docker to run PostgreSQL, Redis, RustFS)

### 1. Clone

```bash
git clone https://github.com/sofn/ArchSmith.git
git clone https://github.com/sofn/ArchSmithAdmin.git
```

### 2. Start Backend

```bash
cd ArchSmith
JAVA_HOME=/path/to/jdk25 ./gradlew server-admin:bootRun
```

> Dev profile auto-starts PostgreSQL, Redis, and RustFS via Testcontainers. No manual DB setup needed.

### 3. Start Frontend

```bash
cd ArchSmithAdmin
pnpm install && pnpm dev
```

### 4. Open Browser

Visit `http://localhost:8848` and login with `admin / admin123`.

### Docker (Alternative)

```bash
cd ArchSmith/docker
./start.sh          # JVM mode (default, Project Leyden CDS)
./start.sh native   # Native Image mode (Liberica NIK 25)
```

## Project Structure

```
ArchSmith (Backend)
├── common/              # Shared utilities & error handling
├── infrastructure/      # Auth, filters, file storage, response wrapper
├── domain/admin-user/   # Domain entities & business logic
├── server-admin/        # Web layer & Spring Boot app
├── dependencies/        # Centralized version management
└── docker/              # Docker & deployment configs
    ├── jvm/             # Leyden CDS optimized Dockerfile
    └── native/          # Liberica NIK 25 native Dockerfile

ArchSmithAdmin (Frontend)
├── src/api/             # API definitions
├── src/views/system/    # System management pages
├── src/views/monitor/   # Monitoring pages
├── src/store/           # Pinia stores
└── src/router/          # Dynamic routing
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 25, Spring Boot 4.0.5, Spring Security, Spring Data JPA, QueryDSL |
| Frontend | Vue 3.5, Vite 8, TypeScript 6, Element Plus, TailwindCSS 4 |
| Database | PostgreSQL 17 (Testcontainers in dev), Redis, Flyway |
| File Storage | Local filesystem, AWS S3 / RustFS (Testcontainers in dev) |
| Monitoring | Oshi, SpringDoc OpenAPI, Micrometer + OpenTelemetry |
| Build | Gradle 9.4.1, pnpm, Docker, Project Leyden, Liberica NIK 25 |
| Testing | JUnit 6, Spock 2.4, RestClient, Testcontainers |

## Documentation

Full documentation: **[archsmith.lesofn.com](https://archsmith.lesofn.com)**

## License

[MIT](./LICENSE)
