<div align="center">
  <h1>AppForge</h1>
  <p><strong>Modern enterprise admin platform built with Spring Boot 4 + Vue 3</strong></p>
  <p>
    <a href="https://appforge.lesofn.com">Documentation</a> ·
    <a href="https://github.com/sofn/AppForgeAdmin">Frontend Repo</a> ·
    <a href="./README.zh-CN.md">中文</a>
  </p>
  <p>
    <img src="https://img.shields.io/badge/Java-21-blue?logo=openjdk" alt="Java 21" />
    <img src="https://img.shields.io/badge/Spring%20Boot-4.0.5-green?logo=springboot" alt="Spring Boot 4" />
    <img src="https://img.shields.io/badge/Vue-3.5-brightgreen?logo=vuedotjs" alt="Vue 3" />
    <img src="https://img.shields.io/badge/Vite-8-purple?logo=vite" alt="Vite 8" />
    <img src="https://img.shields.io/badge/License-MIT-yellow" alt="MIT" />
  </p>
</div>

---

## What is AppForge?

AppForge is a **production-ready, full-stack admin platform** that combines a Spring Boot 4 backend with a Vue 3 frontend. It provides complete user/role/menu/department management, server monitoring, JWT authentication, and more — all with clean architecture and modern tooling.

> **Why AppForge?** Most similar projects (RuoYi, JeecgBoot) are still on Spring Boot 2.x/3.x. AppForge uses **Spring Boot 4 + Java 21 virtual threads + GraalVM Native Image**, delivering ~100ms startup time and ~50MB memory footprint.

## Features

| Category | Details |
|----------|---------|
| **Auth** | JWT + refresh token, Spring Security, BCrypt password, configurable captcha |
| **RBAC** | Users, roles, menus, departments, button-level permissions |
| **System** | Config management, notice/announcements, operation & login logs |
| **Monitor** | Real-time CPU/memory/JVM/disk monitoring (Oshi), embedded Swagger UI |
| **Database** | Multi-datasource with read/write split, Flyway migration |
| **Deploy** | Docker Compose (Native + JVM), Nginx reverse proxy |
| **Frontend** | vue-pure-admin, Element Plus, TailwindCSS, Pinia, dynamic routing |

## Quick Start

### Prerequisites

- Java 21, Node.js 20+, pnpm 9+

### 1. Clone

```bash
git clone https://github.com/sofn/AppForge.git
git clone https://github.com/sofn/AppForgeAdmin.git
```

### 2. Start Backend

```bash
cd AppForge
./gradlew server-admin:bootRun
```

### 3. Start Frontend

```bash
cd AppForgeAdmin
pnpm install && pnpm dev
```

### 4. Open Browser

Visit `http://localhost:8848` and login with `admin / admin123`.

### Docker (Alternative)

```bash
cd AppForge/scripts
./start.sh          # Native mode (default, GraalVM)
./start.sh jvm      # JVM mode
```

## Project Structure

```
AppForge (Backend)
├── common/              # Shared utilities & error handling
├── infrastructure/      # Auth, filters, response wrapper
├── domain/admin-user/   # Domain entities & business logic
├── server-admin/        # Web layer & Spring Boot app
├── dependencies/        # Centralized version management
└── scripts/             # Docker & deployment configs

AppForgeAdmin (Frontend)
├── src/api/             # API definitions
├── src/views/system/    # System management pages
├── src/views/monitor/   # Monitoring pages
├── src/store/           # Pinia stores
└── src/router/          # Dynamic routing
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 4.0.5, Spring Security, Spring Data JPA |
| Frontend | Vue 3.5, Vite 8, TypeScript 6, Element Plus, TailwindCSS 4 |
| Database | MySQL 8 (prod), H2 (dev), Redis, Flyway |
| Monitoring | Oshi, SpringDoc OpenAPI, Micrometer + OpenTelemetry |
| Build | Gradle 9.4.1, pnpm, Docker, GraalVM Native Image |
| Testing | JUnit 6, Spock 2.4, Testcontainers |

## Documentation

Full documentation: **[appforge.lesofn.com](https://appforge.lesofn.com)**

## License

[MIT](./LICENSE)
