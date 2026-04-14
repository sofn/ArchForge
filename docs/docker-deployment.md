# Docker 部署说明

## 部署模式

提供两种 Docker Compose 构建模式：

| 模式 | 文件 | 优势 | 适用场景 |
|------|------|------|---------|
| **Native (默认)** | `docker-compose.native.yml` | 启动快 (~100ms)、内存低 (~50MB) | 生产部署 |
| **JVM** | `docker-compose.yml` | 构建快、兼容性好 | 开发测试 |

## 架构

```
┌─────────────┐     ┌──────────────┐     ┌───────────┐
│  浏览器      │────>│  Nginx (:80) │────>│ AppForge  │
│             │     │  前端静态文件  │     │  (:8080)  │
│             │     │  /api/* 反代  │     │           │
└─────────────┘     └──────────────┘     └─────┬─────┘
                                               │
                                    ┌──────────┴──────────┐
                                    │                     │
                              ┌─────┴─────┐         ┌────┴────┐
                              │ MySQL (:3306)│       │Redis(:6379)│
                              └───────────┘         └─────────┘
```

## 快速启动

```bash
cd scripts

# 默认使用 Native 模式
./start.sh

# 指定模式
./start.sh native   # GraalVM Native Image
./start.sh jvm      # 标准 JVM
```

或直接使用 docker compose：

```bash
cd scripts

# Native 模式
docker compose -f docker-compose.native.yml up -d --build

# JVM 模式
docker compose -f docker-compose.yml up -d --build
```

## 环境变量

复制 `.env.example` 为 `.env` 并按需修改：

```bash
cp .env.example .env
```

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `DB_ROOT_PASSWORD` | `123` | MySQL root 密码 |
| `JWT_SECRET` | (内置) | JWT 签名密钥 |

## 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 (Nginx) | 80 | 访问管理后台 |
| 后端 (AppForge) | 8080 | REST API |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |

## 文件结构

```
scripts/
├── docker-compose.yml          # JVM 模式
├── docker-compose.native.yml   # Native 模式（默认）
├── nginx/
│   └── default.conf            # Nginx 反向代理配置
├── start.sh                    # 启动脚本
├── .env.example                # 环境变量模板
└── logs/                       # 日志目录（运行时创建）
```

## Native vs JVM 对比

| 指标 | Native | JVM |
|------|--------|-----|
| 首次构建时间 | ~10 分钟 | ~2 分钟 |
| Docker 镜像大小 | ~100MB | ~350MB |
| 启动时间 | ~100ms | ~10s |
| 运行内存 | ~50MB | ~300MB |
| 吞吐量 | 略低 | 高 |
| 调试支持 | 有限 | 完整 |
