# 数据库迁移 (Flyway)

## 概述

项目使用 [Flyway](https://flywaydb.org/) 管理数据库表结构迁移，确保 test/prod 环境数据库结构一致、可追溯、可回滚。

## 环境策略

| 环境 | DDL 管理 | 数据初始化 | Flyway |
|------|---------|-----------|--------|
| **dev** | Hibernate DDL auto=update | InitDbMockServer (H2 seed SQL) | 禁用 |
| **test** | Flyway migration | Flyway V2 seed data | 启用 |
| **prod** | Flyway migration | Flyway V2 seed data (首次) | 启用 |

## 迁移脚本

脚本位于 `server-admin/src/main/resources/db/migration/`：

```
db/migration/
├── V1__init_schema.sql       # 建表（所有业务表）
├── V2__init_seed_data.sql    # 基础数据（用户、角色、部门、参数、公告）
├── V3__init_menu_data.sql    # 菜单和角色菜单数据
└── V4__xxx.sql               # 后续增量迁移...
```

### 命名规范

```
V{版本号}__{描述}.sql
```

- `V` 前缀 + 版本号（递增整数）
- 双下划线 `__` 分隔
- 描述使用 snake_case

### 编写规则

1. **只增不改**: 已执行的迁移脚本不可修改
2. **幂等设计**: 使用 `CREATE TABLE IF NOT EXISTS`
3. **兼容性**: 新增列使用 `DEFAULT` 值，不删除已有列
4. **编码**: UTF-8

## Test 环境操作指南

### 首次部署

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE appforge_test DEFAULT CHARSET utf8mb4;"

# 2. 配置环境
cd server-admin/src/main/resources
cp application-test.yaml.example application-test.yaml
# 编辑 application-test.yaml，填入数据库连接信息

# 3. 启动应用（Flyway 自动执行迁移）
SPRING_PROFILES_ACTIVE=test ./gradlew server-admin:bootRun
```

### 增量更新

```bash
# 1. 添加新的迁移脚本
# server-admin/src/main/resources/db/migration/V4__add_xxx_table.sql

# 2. 重启应用，Flyway 自动检测并执行新脚本
SPRING_PROFILES_ACTIVE=test ./gradlew server-admin:bootRun
```

### 查看迁移状态

```sql
-- 查看 Flyway 迁移历史
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

### 手动执行迁移（不启动应用）

```bash
# 使用 Gradle Flyway 插件（需额外配置）或直接用 Flyway CLI
flyway -url=jdbc:mysql://localhost:3306/appforge_test \
       -user=root -password=xxx \
       -locations=classpath:db/migration \
       migrate
```

## Prod 环境操作指南

### 首次部署

```bash
# 1. 创建数据库（建议主从分别创建）
mysql -h <master-host> -u root -p -e "CREATE DATABASE appforge DEFAULT CHARSET utf8mb4;"

# 2. 配置环境
cd server-admin/src/main/resources
cp application-prod.yaml.example application-prod.yaml
# 编辑：
#   - 数据库连接（master/slave）
#   - Redis 连接
#   - JWT 密钥（务必更换！）
#   - app-forge.flyway.enabled: true

# 3. 启动（Flyway 自动建表 + 初始化数据）
SPRING_PROFILES_ACTIVE=prod java -jar server-admin.jar
```

### 数据库变更流程

```
1. 开发者编写迁移脚本
   └─ server-admin/src/main/resources/db/migration/V{N}__描述.sql

2. dev 环境验证
   └─ H2 + Hibernate DDL auto 验证实体兼容性

3. test 环境验证
   └─ 部署到 test，Flyway 自动执行，验证 SQL 正确性

4. prod 部署
   └─ 发布新版本，Flyway 自动执行增量迁移
```

### 生产注意事项

1. **备份**: 执行迁移前务必备份数据库
2. **审核**: 迁移脚本需经过 Code Review
3. **回滚**: Flyway 社区版不支持自动回滚，需手动编写回滚 SQL
4. **大表变更**: ALTER TABLE 大表时考虑 `pt-online-schema-change`
5. **密钥安全**: `application-prod.yaml` 已 gitignore，勿提交

### 回滚操作

```bash
# 手动回滚（编写反向 SQL）
mysql -h <master-host> -u root -p appforge < rollback_V4.sql

# 修复 Flyway 记录
mysql -h <master-host> -u root -p appforge -e \
  "DELETE FROM flyway_schema_history WHERE version = '4';"
```

## Docker 部署

Docker Compose 中已配置环境变量传递数据库连接信息：

```bash
cd scripts

# JVM 模式
docker compose up -d --build

# Native 模式
docker compose -f docker-compose.native.yml up -d --build
```

Flyway 会在应用启动时自动执行迁移。

## 配置参考

### test 环境 (application-test.yaml)

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate    # Flyway 管理 DDL，Hibernate 仅验证

app-forge:
  flyway:
    enabled: true            # 启用 Flyway
```

### prod 环境 (application-prod.yaml)

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate    # Flyway 管理 DDL，Hibernate 仅验证

app-forge:
  flyway:
    enabled: true            # 启用 Flyway
```

### dev 环境 (application-dev.yaml)

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update       # Hibernate 自动建表（H2）

app-forge:
  flyway:
    enabled: false           # dev 禁用 Flyway
  embedded:
    h2-init: true            # 使用 InitDbMockServer 加载种子数据
```
