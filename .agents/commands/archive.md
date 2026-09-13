# /archive —— 变更归档与知识沉淀

变更：`.agents/changes/$ARGUMENTS/`

## 流程
1. 确认 spec status=done，tasks 全部完成，execution-log 有最终验证证据
2. 沉淀检查：
   - 新踩坑 → `memory/pitfalls/<主题>.md`（真实故障+根因+规避），索引补一行
   - 长期决策 → `memory/decisions.md`
   - 可复用技术知识 → `knowledge/tech-<主题>.md` + index.md 一行索引
   - 规范类发现 → `AGENTS.md` §5 或 `docs/specs/`（**不要写进 pitfalls**）
   - 架构决策 → `docs/adr/`（为什么是ADR而非spec：ADR记"为什么"，spec记"是什么"）
3. 变更目录移入 `.agents/changes/archive/`（或 spec 头标 status: done）
4. git commit 变更档案与代码同 PR 或紧随其后的 docs 提交
