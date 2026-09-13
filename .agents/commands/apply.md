# /apply —— 按确认的 Spec 执行

变更：`.agents/changes/$ARGUMENTS/`

## 前置
- 读 `spec.md` + `tasks.md`；spec status 必须是 confirmed
- 用户已批准执行

## 零偏差原则
- Plan 是合同，AI 是打印机 —— 不偏离 spec
- 发现 spec 与实际不符：先改 spec 再改代码（Reverse Sync）

## 逐 Task 执行
1. 按 tasks.md 顺序执行，每 task 原子化（3-5 文件）
2. 每 task 完成后跑对应验证（AGENTS.md per-edit 协议）：
   - 主代码：`./gradlew :<module>:compileJava :<module>:checkstyleMain`
   - 测试代码：`compileTestJava` + `checkstyleTest`
   - 行为变更：`./gradlew :<module>:test --tests '*XxxTest*'`
3. 证据追加进 `execution-log.md`（不空写"已通过"）
4. tasks.md 只更新本轮 task 状态

## 收尾
- `./gradlew spotlessApply` 后跑受影响范围测试
- 全量 `./gradlew build`（共享基建变更必须）
- spec.md §12 执行日志回填
