# 踩坑：认证 / sa-token / 权限

## sa-token 通配符 `*:*:*` 只按段字面匹配

**发现日期**: 2026-09-13

**问题描述**:
admin 角色持有 `ALL_PERMISSIONS = "*:*:*"`，但 `@SaCheckPermission("meta-table:list")` 全部 403。jshell 实测 `StpUtil` 的 `vagueMatch`：`"*:*:*"` 是三段式模式，只命中三段式权限（`system:user:list`），**不匹配单段/两段式**（`meta-table:list`）。结果：meta-table 全部端点对超管不可达。

**根因**:
sa-token 的 vagueMatch 按 `:` 分段逐段匹配 `*`，不是"任意后缀"语义。全局通配应写 `"*"`。

**解决方案**:
- `RoleInfo.ALL_PERMISSIONS` 用 `"*"`（sa-token 官方超管惯例）
- 前端 `hasPerms`/`v-perms` 同时识别 `"*"` 与 `"*:*:*"` 保老 token 兼容（token 有效期内滚动）
- 修复 commit：`61d4697e`

## LoginContext 在 sa-token context 未初始化时抛异常

**发现日期**: 2026-09-13

**问题描述**:
请求日志 enricher 在外层 servlet filter 里调 `StpAdminUtil.isLogin()`，sa-token context 此时尚未绑定 → `SaTokenContextException` → 全站 500（连 /auth/getConfig 都挂）。

**根因**:
sa-token 的 `StpUtil` 依赖 SaTokenContext（由 sa-token 自己的 servlet filter/interceptor 建立）。在更外层的 filter 或异步线程里调用会炸。

**解决方案**:
- `LoginContext.findAdminUser()` 加 try-catch 降级返回 null（commit `032fe8a7`）
- 外层 filter 场景**不要依赖 LoginContext**，用 `StpAdminUtil.STP_LOGIC.getLoginIdByToken(token)` 走 dao 直解（无需请求上下文）
- 教训：任何在 sa-token filter 之前执行的组件都不能碰 `StpUtil.isLogin()`

## 请求日志 uid 用 token 直解而非请求上下文

**发现日期**: 2026-09-13

**问题描述**:
`RequestContextEnricher` 读 `RequestContext.currentUid` 恒为 0 —— 该字段唯一写入方 `AuthResourceFilter` 是死代码（`@Service` 被注释、非 prod 短路）。

**根因**:
sa-token 迁移后 uid 由 `LoginContext`（session）承载，但 RequestContext 旧字段无人再写。外层 filter 又拿不到 LoginContext（见上条）。

**解决方案**:
- 日志 enricher 从 `Authorization: Bearer` 头取 token → `getLoginIdByToken` 直解 uid（commit `9088cf02`）
- 顺带暴露更大问题：meta-table 全部写路径 `creator_id/updater_id` 恒 0（`efc979d7` 修）；遗留 auth 栈整体删除（`032fe8a7`）
