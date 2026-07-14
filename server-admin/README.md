打包模块，包含 Spring 全局配置

### 环境配置
* dev     本地开发环境 配置： `application-dev.yaml`（使用 `scripts/dev/init.sh` 启动本地 Docker 容器）
* test    测试环境     配置： `application-test.yaml`（默认 Testcontainers + Flyway）
* staging 预发环境     配置： `application-staging.yaml`（外部 PostgreSQL / Redis + Flyway）
* prod    线上环境     配置： `application-prod.yaml`（外部 PostgreSQL / Redis + Flyway）

`src/main/resources/application.yaml` 放置公共配置；`SPRING_PROFILES_ACTIVE` 决定激活哪个 profile。

* 单测/集成测试默认通过 `server-admin/build.gradle.kts` 注入 `test` profile。
* 开发环境通过 `server-admin:bootRun` 启动时默认使用 `dev` profile。