<div align="center">
  <h1>AppForge</h1>
  <p><strong>基于 Spring Boot 4 + Vue 3 的现代企业级管理平台</strong></p>
  <p>
    <a href="https://appforge.lesofn.com">在线文档</a> ·
    <a href="https://github.com/sofn/AppForgeAdmin">前端仓库</a> ·
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

AppForge 是一个**开箱即用的全栈管理系统**，后端基于 Spring Boot 4，前端基于 Vue 3，提供完整的用户/角色/菜单/部门管理、文件上传下载、服务器监控、JWT 认证等功能。采用整洁架构，使用现代化技术栈。

### 与同类项目对比

| 特性 | AppForge | RuoYi | JeecgBoot |
|------|----------|-------|-----------|
| Spring Boot | **4.0.5** | 2.x | 3.x |
| Java | **25 (虚拟线程)** | 8/17 | 17 |
| Native Image | **支持 (~100ms启动)** | 不支持 | 不支持 |
| 前端框架 | **Vue 3 + Vite 8** | Vue 2/3 | Vue 3 |
| 数据库 | **PostgreSQL** | MySQL | MySQL |
| 数据库迁移 | **Flyway** | 手动SQL | 手动SQL |
| 文件存储 | **本地 + S3 (MinIO)** | 本地 | 本地/OSS |
| 服务器监控 | **Oshi 实时监控** | Sigar | 无 |
| 部署方式 | **Docker (Leyden + Native)** | Docker | Docker |

## 功能模块

| 模块 | 说明 |
|------|------|
| 用户管理 | 用户增删改查、部门树筛选、状态切换、密码重置、角色分配 |
| 角色管理 | 角色增删改查、菜单权限分配、按钮级权限控制 |
| 菜单管理 | 动态菜单树、多级菜单、iframe/外链支持 |
| 部门管理 | 组织架构树形管理 |
| 文件管理 | 文件/图片上传下载，支持本地存储和 S3 (MinIO) |
| 参数设置 | 系统参数配置管理 |
| 通知公告 | 通知/公告发布管理 |
| 日志管理 | 操作日志、登录日志查看与清理 |
| 服务监控 | CPU/内存/JVM/磁盘实时监控仪表盘 |
| 接口文档 | 内嵌 Swagger UI (SpringDoc OpenAPI) |

## 快速开始

### 环境要求

- Java 25、Node.js 20+、pnpm 9+、Docker（Testcontainers 需要）

### 1. 克隆项目

```bash
git clone https://github.com/sofn/AppForge.git
git clone https://github.com/sofn/AppForgeAdmin.git
```

### 2. 启动后端

```bash
cd AppForge
JAVA_HOME=/path/to/jdk25 ./gradlew server-admin:bootRun
```

> 开发环境自动通过 Testcontainers 启动 PostgreSQL、Redis、MinIO，无需手动安装。

### 3. 启动前端

```bash
cd AppForgeAdmin
pnpm install && pnpm dev
```

### 4. 访问系统

浏览器打开 `http://localhost:8848`，使用 `admin / admin123` 登录。

### Docker 部署

```bash
cd AppForge/docker
./start.sh          # JVM 模式（默认，Project Leyden CDS 优化）
./start.sh native   # Native Image 模式（Liberica NIK 25）
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 25, Spring Boot 4.0.5, Spring Security, Spring Data JPA, QueryDSL |
| 前端 | Vue 3.5, Vite 8, TypeScript 6, Element Plus, TailwindCSS 4 |
| 数据库 | PostgreSQL 17 (开发环境 Testcontainers), Redis, Flyway |
| 文件存储 | 本地文件系统, AWS S3 / MinIO (开发环境 Testcontainers) |
| 监控 | Oshi, SpringDoc OpenAPI, Micrometer + OpenTelemetry |
| 构建 | Gradle 9.4.1, pnpm, Docker, Project Leyden, Liberica NIK 25 |
| 测试 | JUnit 6, Spock 2.4, RestClient, Testcontainers |

## 文档

完整文档请访问: **[appforge.lesofn.com](https://appforge.lesofn.com)**

## 许可证

[MIT](./LICENSE)
