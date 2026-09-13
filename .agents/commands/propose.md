# /propose —— 创建变更提案

需求描述：$ARGUMENTS

## 核心法则
1. **No Spec, No Code** — 没有确认的 spec 不动代码
2. **Spec is Truth** — spec 与代码冲突时，错的是代码
3. **代码现状必须有出处** — 每条结论标注 `文件路径 + 类名/方法名`，禁止"我认为"/"通常来说"

## 执行步骤

### 一、Research（代码现状调查）
- 读 `AGENTS.md`、`.agents/memory/pitfalls.md` 索引（按主题打开相关分类）
- 找出涉及的模块/类/方法，标注出处
- 若匹配 `.agents/skills/` 中某 skill 的触发条件，先读对应 SKILL.md

### 二、逐个澄清
- 一次只问一个问题，给选项 + 推荐方案
- YAGNI 裁剪（只做必要功能）

### 三、生成 Spec
按 `.agents/changes/_templates/spec.md` 写入 `.agents/changes/<date>-<topic>/spec.md`，分段生成、每段确认。

产出物：`spec.md` + `tasks.md`（任务拆分见模板）。完成后等用户批准进入 /apply。
