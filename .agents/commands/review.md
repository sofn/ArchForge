# /review —— 两阶段审查

变更：`.agents/changes/$ARGUMENTS/`

## 阶段一：Spec Compliance（先行门禁）
逐条对照 spec.md §3 功能点与 §4 业务规则：
- 缺失实现（spec 要求但没做）
- 多余实现（spec 没要求但做了 —— YAGNI 违规）
- 理解偏差（做了但方向错）
- 数据/接口变更与 spec §5/§6 是否一致
- `spec/openapi.yaml` 是否同步

**不信报告，只信代码** —— 逐条给出 `文件:行号` 证据。输出 ✅/❌/⚠️ 逐功能点结论。
阶段一 PASS 才进阶段二。

## 阶段二：Code Quality
读 `docs/specs/` + `AGENTS.md` 规范，按三级输出：
- **Critical（阻塞）**：安全漏洞、并发安全、数据丢失、事务边界错误
- **Important（应修）**：异常吞掉、缺参数校验、魔法值、越界依赖
- **Minor（建议）**：命名、注释、import 清理

## 输出
审查结论回填 spec.md §13；新踩坑写入 `.agents/memory/pitfalls/<主题>.md`。
