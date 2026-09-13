# 知识索引
> 专题知识的轻量索引。每条一句话说清核心逻辑。
> 格式：- **触发关键词**: 一句话核心逻辑 → `文件名`（可选）

## 技术约定
- **sa-token 通配语义**: `*` 全局匹配，`a:*` 段内匹配，`*:*:*` 只命中三段式 → `../memory/pitfalls/auth.md`
- **ScopedValue 请求上下文**: `ScopedValueContext.getRequestContext()` 取值，`runInScope` 绑定；替代 ThreadLocal → `skills/archforge-project-standard/standard.md`
- **meta-table 审计列约定**: id identity PK + creator_id/create_time/updater_id/update_time/deleted 五列固定设施，不生成 MetaColumn → `../skills/archforge-metatable/SKILL.md`
- **请求日志 TSV 契约**: CUSTOM_LOG MDC 标记 + TSV 字段序，脱敏在 filter 层完成 → `archforge-request-log-starter`
- **Starters 装配惯例**: `@AutoConfiguration` + `META-INF/spring/...AutoConfiguration.imports`，经 infrastructure `api(project)` 被 server 消费

## 踩坑记录
真实故障按主题写在 `.agents/memory/pitfalls/`，先看 `.agents/memory/pitfalls.md` 索引。
