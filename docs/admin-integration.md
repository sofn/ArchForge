# AppForgeAdmin 集成说明

## 概述

AppForgeAdmin 是基于 [vue-pure-admin](https://github.com/pure-admin/vue-pure-admin) 的管理后台 UI 项目，已与 AppForge 后端完成集成，实现了完整的管理系统功能。

## 架构

```
┌──────────────────────┐     ┌──────────────────────┐
│   AppForgeAdmin      │     │   AppForge Backend   │
│   (Vue 3 + Vite)     │────>│   (Spring Boot 4)    │
│   Port: 8848         │     │   Port: 8080         │
│                      │     │                      │
│   Vite Proxy:        │     │   API Endpoints:     │
│   /api/* -> :8080/*  │     │   /login             │
│                      │     │   /refresh-token     │
│                      │     │   /get-async-routes  │
│                      │     │   /user, /role ...   │
└──────────────────────┘     └──────────────────────┘
```

## 功能模块

### 已集成功能

| 模块 | 前端路径 | 后端 API | 说明 |
|------|---------|---------|------|
| 登录 | `/login` | `POST /login` | 用户名密码登录，JWT 认证 |
| 令牌刷新 | - | `POST /refresh-token` | 自动刷新 accessToken |
| 动态路由 | - | `GET /get-async-routes` | 根据角色动态生成菜单 |
| 用户管理 | `/system/user` | `POST /user` | 分页查询、CRUD |
| 角色管理 | `/system/role` | `POST /role` | 分页查询、权限分配 |
| 菜单管理 | `/system/menu` | `POST /menu` | 树形结构管理 |
| 部门管理 | `/system/dept` | `POST /dept` | 树形结构管理 |
| 权限管理 | `/permission` | - | 页面级和按钮级权限控制 |
| 角色权限 | - | `POST /role-menu`, `POST /role-menu-ids` | 角色菜单权限分配 |

### 认证流程

1. 前端发送 `POST /login` (username, password)
2. 后端验证用户名密码（dev 环境不需要验证码，test/prod 需要）
3. 返回 `{ accessToken, refreshToken, expires, roles, permissions, avatar, username, nickname }`
4. 前端存储 token 到 Cookie，后续请求通过 `Authorization: Bearer <token>` 携带
5. Token 过期时自动调用 `/refresh-token` 刷新

### 验证码

| 环境 | 验证码 | 配置 |
|------|--------|------|
| dev | 关闭 | `app-forge.captcha.enabled: false` |
| test | 开启 | `app-forge.captcha.enabled: true` |
| prod | 开启 | `app-forge.captcha.enabled: true` |

### JWT 配置

使用 Spring Security + JJWT 0.12.x：
- 算法: HS512
- dev 过期时间: 7天 (604800秒)
- test 过期时间: 1天 (86400秒)
- prod 过期时间: 1小时 (3600秒)

## 启动方式

### 后端

```bash
cd AppForge
export JAVA_HOME=/path/to/jdk21
./gradlew server-admin:bootRun
```

后端启动在 `http://localhost:8080`

### 前端

```bash
cd AppForgeAdmin
pnpm install
pnpm dev
```

前端启动在 `http://localhost:8848`，通过 Vite proxy 将 `/api/*` 请求代理到后端 `http://localhost:8080`

### 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 超级管理员 |
| ag1 | admin123 | 普通用户 |

## API 接口

### 公开接口（无需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/login` | 用户登录 |
| POST | `/refresh-token` | 刷新令牌 |
| GET | `/getConfig` | 获取系统配置 |
| GET | `/captchaImage` | 获取验证码 |

### 系统管理接口（需要认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/get-async-routes` | 获取动态路由 |
| POST | `/user` | 用户列表（分页） |
| GET | `/list-all-role` | 全部角色列表 |
| POST | `/list-role-ids` | 用户角色 ID 列表 |
| POST | `/role` | 角色列表（分页） |
| POST | `/role-menu` | 角色权限菜单树 |
| POST | `/role-menu-ids` | 角色已分配菜单 ID |
| POST | `/menu` | 菜单列表（扁平） |
| POST | `/dept` | 部门列表（扁平） |

### 响应格式

```json
{
  "code": 0,
  "message": "操作成功",
  "data": { ... }
}
```

- `code: 0` 表示成功
- 分页响应: `data: { list: [], total: number, pageSize: number, currentPage: number }`

## 数据库

### 新增表

- `sys_dept` - 部门表，SQL: `sql/data-admin-dept.sql`

### 已有表

- `sys_user` - 用户表
- `sys_role` - 角色表
- `sys_menu` - 菜单表
- `sys_role_menu` - 角色菜单关联表

## 测试

```bash
# 运行后端 API 集成测试
./gradlew :server-admin:test
```

测试覆盖所有 API 端点：登录、刷新令牌、用户列表、角色列表、菜单列表、部门列表、角色权限等。

## 前端项目变更

1. **禁用 Mock**: `build/plugins.ts` 中 `vite-plugin-fake-server` 设置 `enable: false`
2. **API 代理**: `vite.config.ts` 配置 proxy 将 `/api/*` 代理到后端
3. **HTTP 基础路径**: `src/utils/http/index.ts` 设置 `baseURL: "/api"`
4. **登录规则简化**: `src/views/login/utils/rule.ts` 移除验证码校验（dev 环境无需验证码）
