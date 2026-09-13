# 踩坑：Flyway / 数据源 / PostgreSQL

## flyway_schema_history 残留已删除迁移 → validate 失败

**发现日期**: 2026-09-13

**问题描述**:
`Flyway validation failed: applied migrations not resolved locally: 4.1, 5` —— 历史库里留有本地已删除的 Quartz 迁移记录。

**根因**:
迁移脚本被删除/改名后，history 表仍记录其已应用，validate 对不上。

**解决方案**:
- 快速恢复：`DELETE FROM flyway_schema_history WHERE version IN ('4.1','5')`（V23 迁移本体已内置同款逻辑）
- 规范做法：`flyway repair`；本地 dev 直接清行即可
- 预防：迁移文件合入后不改名不删除

## PG SCRAM verifier 与容器 env 脱节

**发现日期**: 2026-09-13

**问题描述**:
`FATAL: password authentication failed for user "archforge"` —— 容器 env `POSTGRES_PASSWORD` 是 X，但角色 verifier 是早前手动改过的 Y。

**根因**:
`docker exec ... psql -h localhost` 走 local trust 不验证密码，掩盖了 verifier 脱节；真实 TCP 连接才走 SCRAM。

**解决方案**:
- 验证密码必须用容器 IP 走 host 规则：`psql -h $(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' docker-postgres-1)`，localhost 不算数
- 对齐：`ALTER USER archforge PASSWORD '<env 值>'`
