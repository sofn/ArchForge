# .agents/changes/ —— 变更档案工作流

每个非琐碎变更一个目录：`.agents/changes/<date>-<topic>/`

## 文件四件套（模板在 `_templates/`）

| 文件 | 内容 |
|---|---|
| `spec.md` | 背景目标 / 代码现状（每条结论带 `文件路径+类名.方法名` 出处）/ 功能点 / 业务规则 / 数据与接口变更 / 风险 / 待澄清 / HARD-GATE 确认记录 |
| `tasks.md` | 任务拆分，顺序：数据模型→接口协议→底层实现→上层编排→入口层；每 task 精确到文件路径+函数签名，原子化 3-5 文件 |
| `test-spec.md` | 本轮验证范围矩阵、用例、跳过项 |
| `execution-log.md` | 每轮追加：时间 / 变更范围 / 命令+结果 / 接口或 DB 结论 / 警告是否阻断 / 跳过项原因 / 本轮启动服务 PID |
| `verification/` | 截图、日志等证据附件（可选） |

## 规则

- **No Spec No Code**：spec 未经确认不动代码（琐碎修复/文档除外）
- **Spec is Truth**：执行中发现 spec 与实际不符，先修 spec 再修代码
- **增量验证**：复用已有 test-spec/execution-log，本轮只追加差异部分，禁止重写基线
- **证据优先**：不许只写"已通过"，必须留实际命令与关键输出
- 完成后目录移入 `archive/` 或在 spec 头标 `status: done`

## 验证矩阵（默认最小集，按变更类型）

| 变更类型 | 必跑 | 条件增强 |
|---|---|---|
| 仅文档/spec | `git diff --check` | 链接/状态一致性 |
| Java 后端 | 相关模块 `compileJava` + `checkstyleMain` | 业务逻辑→单测；装配→受影响模块 build |
| Flyway SQL | `${}` 占位符静态扫描（应无输出）+ 启动实跑 | dev 库查 flyway_schema_history |
| 前端 | `pnpm typecheck` + 触及文件 lint | UI 交互→dev server 浏览器验证 |
| API 协议 | `spec/openapi.yaml` 同步 + 启动 + curl 实测 | 鉴权相关→普通+内部调用边界 |
| 共享基建（auth/datasource/context） | `./gradlew build` 全门禁 | 双应用 bootRun 冒烟 |

默认不全量 `build`/E2E；共享基础能力、权限、数据迁移变更才升级。
