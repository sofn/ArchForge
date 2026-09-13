# ArchForge 长期决策记录

> 架构/产品/技术路线的长期选择。格式：日期 + 决策 + 理由。新条目追加在顶部。

## 2026-09-13 —— AI 资产单源 `.agents/` + 各工具薄适配

AI 编程资产（skills/commands/agents/memory/changes）统一放 `.agents/` 作为唯一权威目录；各工具（Devin/Claude Code/Codex/OpenCode）只做 ≤3 行指针适配，禁止双份维护。参考 forge-admin 的 `.agents/skills` 单源惯例及其 `.opencode/memory` 与 `code-copilot/memory` 双源漂移教训。

## 2026-09-13 —— plan 从外部 codeplans 迁入 `.agents/changes/`

变更档案（spec/tasks/test-spec/execution-log）与代码同仓同 PR。原 `../codeplans/ArchForge/*.md` 约定废弃（跨仓引用脆弱：clone 断链、曾发生无 remote scratch 仓库误写）。codeplans 仓库保留给跨仓研究报告（reports/analyze）。

## 2026-09-13 —— sa-token 超管通配统一用 `*`

`RoleInfo.ALL_PERMISSIONS = "*"`；前端 hasPerms/v-perms 兼容 `"*"` 与 `"*:*:*"`（老 token 滚动期）。见 pitfalls/auth.md。

## 2026-09-13 —— meta-table 导入采用"纳管制+严格兼容门"

仅允许结构完全合规的表（id identity PK + 5 审计列 + 可映射类型 + 平台表黑名单）；不 ALTER 不改数据。妥协：本系统表（user_id PK 惯例）不可导入，只服务真正的 meta_ 规范表与刻意兼容的外部表。
