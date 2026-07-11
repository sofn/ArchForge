<div align="center">
  <h1>ArchForge</h1>
  <p><strong>基于 Spring Boot 4 + Vue 3 的现代企业级管理平台</strong></p>
  <p>
    <a href="https://archforge.lesofn.com">在线文档</a> ·
    <a href="https://github.com/sofn/ArchForgeAdmin">前端仓库</a> ·
    <a href="./README.md">English</a>
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

## 项目简介

ArchForge 是一个**开箱即用的全栈管理系统**，后端基于 Spring Boot 4，前端基于 Vue 3，提供完整的用户/角色/菜单/部门管理、文件管理、定时任务、国际化、日志审计、服务器监控、JWT 认证等功能。采用整洁架构，使用现代化技术栈。

### 为什么选择 ArchForge？

- **架构先进**：JDK 25 + Spring Boot 4 + DDD + 整洁架构，不是老项目的升级版
- **团队标准**：规范化约定（Spotless、JSpecify、Lombok），统一依赖 BOM，Skill 化入门指引
- **JDK 25 新能力**：ScopedValue、结构化并发、模式匹配、Stream Gatherers、虚拟线程
- **生产就绪**：Docker（jlink 最小 JRE + Leyden CDS）、Flyway 迁移、多数据源、Micrometer 可观测性
- **零配置开发**：`./gradlew server-admin:bootRun` 自动通过 Testcontainers 启动 PostgreSQL、Redis、RustFS

## 功能模块

| 模块 | 说明 |
|------|------|
| 用户管理 | 用户增删改查、部门树筛选、状态切换、密码重置、角色分配 |
| 角色管理 | 角色增删改查、菜单权限分配、按钮级权限控制 |
| 菜单管理 | 动态菜单树、多级菜单、iframe/外链支持 |
| 部门管理 | 组织架构树形管理 |
| 文件管理 | 文件上传、列表、下载、删除；支持本地存储与 S3 (RustFS)；扩展名/大小/MIME 白名单 |
| 参数设置 | 系统参数配置管理 |
| 通知公告 | 通知/公告发布管理 |
| 日志管理 | 操作日志、登录日志查看与清理 |
| 定时任务 | 基于 Quartz 的反射式 Cron 任务；支持暂停/恢复/立即执行/执行日志 |
| 国际化 | 前后端 locale 同步，默认支持简体中文与英文消息 |
| 服务监控 | CPU/内存/JVM/磁盘实时监控仪表盘，Druid SQL 监控 |
| 接口文档 | 内嵌 Swagger UI (SpringDoc OpenAPI) |
| 架构 | Spring Modulith 2.0 模块边界、显式 DTO 接口、JSpecify 空安全、Spotless 代码格式化 |
| 安全 | 管理端接口强制鉴权、旧接口下线、敏感信息脱敏、@RepeatSubmit 防重放 |

## 新增能力

本次合并的 feature/dev 分支带来了一批可直接落地的能力：

- **文件管理** — 完整支持上传、列表、下载、删除，可配置本地或 S3 (RustFS) 存储，支持扩展名白名单、文件大小与 MIME 类型限制。
- **定时任务** — 基于 Quartz 的反射式 Cron 任务，管理后台可直接暂停/恢复/立即执行，并查看执行日志。
- **国际化** — 后端 Spring MessageSource 与前端 vue-i18n 联动，默认提供简体中文与英文两套消息。
- **Druid 监控** — 非生产环境开启 Druid SQL 监控，生产环境自动隐藏。
- **Spring Modulith 2.0** — 显式模块边界、依赖关系校验与模块文档生成测试，支撑 `admin-user`、`example-task` 与 infrastructure 层。
- **安全加固** — 管理端接口要求 admin 或已认证身份，下线历史接口，敏感日志脱敏，并支持 `@RepeatSubmit` 防重放。
- **显式 DTO 接口** — 控制器逐步替换 `Map` 参数与返回值，改为显式 Request/Response DTO，配合 MapStruct 映射。
- **体验优化** — QueryHelp 适配 Druid `mergeSql` 的 `LIKE` 转义，JDK 25 原生访问为测试与 AOT 任务开启。

## 快速开始

### 环境要求

- Java 25、Node.js 20+、pnpm 9+
- **Docker**（开发模式必需 — Testcontainers 通过 Docker 运行 PostgreSQL、Redis、RustFS）

### 1. 克隆项目

```bash
git clone https://github.com/sofn/ArchForge.git
git clone https://github.com/sofn/ArchForgeAdmin.git
```

### 2. 启动后端

```bash
cd ArchForge
JAVA_HOME=/path/to/jdk25 ./gradlew server-admin:bootRun
```

> 开发环境自动通过 Testcontainers 启动 PostgreSQL、Redis、RustFS，无需手动安装。

### 3. 启动前端

```bash
cd ArchForgeAdmin
pnpm install && pnpm dev
```

### 4. 访问系统

浏览器打开 `http://localhost:8848`，使用 `admin / admin123` 登录。

### Docker 部署

```bash
cd ArchForge/docker
./start.sh          # JVM 模式（默认，Project Leyden CDS 优化）
./start.sh native   # Native Image 模式（Liberica NIK 25）
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 25, Spring Boot 4.0.5, Spring Security, Spring Data JPA, QueryDSL, Spring Modulith 2.0 |
| 前端 | Vue 3.5, Vite 8, TypeScript 6, Element Plus, TailwindCSS 4 |
| 数据库 | PostgreSQL 17 (开发环境 Testcontainers), Redis, Flyway |
| 文件存储 | 本地文件系统, AWS S3 / RustFS (开发环境 Testcontainers) |
| 监控 | Oshi, SpringDoc OpenAPI, Micrometer + OpenTelemetry |
| 构建 | Gradle 9.4.1, pnpm, Docker, Project Leyden, Liberica NIK 25 |
| 测试 | JUnit 6, Spock 2.4, RestClient, Testcontainers |

## 文档

完整文档请访问: **[archforge.lesofn.com](https://archforge.lesofn.com)**

## 许可证

[MIT](./LICENSE)
