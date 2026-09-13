# 用户偏好

> 用户明确表达过的偏好。新会话开始先读本文件。

- 提交信息不带 Co-Authored-By（三仓 AGENTS.md 统一约定）
- 每个任务结束必须调 ask_user_question 询问下一步，不要直接结束回合
- 中文交流，技术术语可保留英文
- plan/报告落盘到 `codeplans` 仓（跨仓研究）或 `.agents/changes/`（单仓变更档案）
- 验证优先：改动后跑对应门禁，build 前不过度声明完成
