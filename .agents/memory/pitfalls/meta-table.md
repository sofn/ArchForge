# 踩坑：meta-table / 内省 / 导入

## `*Mapper` 类名撞 ArchUnit 命名规则

**发现日期**: 2026-09-13

**问题描述**:
`PgTypeMapper`（PG 类型 → MetaColumnType 的纯函数映射器）编译被 ArchUnit `noMapperNamedClasses` 规则拦截 —— 项目保留 `*Mapper` 给 MyBatis/MapStruct 语义。

**解决方案**:
非 ORM 映射器用 `*Mapping`/`*Resolver` 命名（`PgTypeMapping`）。写新类前过一眼 ArchUnit 规则集。

## 平台表黑名单必须与命名规则解耦

**发现日期**: 2026-09-13

**问题描述**:
meta-table 导入已有表的兼容判定只靠 `validateTableCode` 拦 `sys_/qrtz_/pg_` —— 但 `meta_`（自家前缀必须放行）与 `flyway_` 裸奔，结构合规的平台表可被 `meta-table:add` 持有者纳管并经 list 导出敏感行。

**根因**:
命名校验是格式规则不是安全边界 —— 两个职责被合并了。

**解决方案**:
- 独立 `PLATFORM_TABLE_PREFIXES`（sys_/meta_/qrtz_/flyway_）双层防御：compatibilityReasons 给原因 + `importTable` 独立 fail-fast 守卫（不依赖兼容判定链，防重构绕过）
- 安全类检查永远单独成层，不要"顺便"复用命名/格式校验
- 修复：`6ad81904`
