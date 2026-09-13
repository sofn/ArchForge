# /test —— 增量测试与验证

变更：`.agents/changes/$ARGUMENTS/`

## 原则
- Red/Green：测试先红后绿
- **复用基线**：先读已有 `test-spec.md`/`execution-log.md`，只追加本轮差异，禁止从零重规划
- 验证矩阵见 `.agents/changes/README.md`（按变更类型选最小集）

## 流程
1. 读 spec/tasks + 本轮 `git status`/`git diff --name-only` 定增量范围
2. 更新 test-spec.md（已有则追加"本轮增量验证"节）
3. 执行：低成本检查先行（diff-check/编译/静态门禁）→ 单测 → 条件 IT（`@Tag("slow")` 需 Docker）
4. execution-log.md 追加证据：时间/范围/命令+结果/接口或DB结论/警告/跳过原因/服务PID
5. 回填 tasks.md 本轮 task 状态 + spec.md §12
6. 新踩坑 → `memory/pitfalls/<主题>.md` + 索引一行

## 常用命令
```bash
./gradlew :<module>:test --tests '*XxxTest*'          # 单测
./gradlew :archforge-server-admin:test --tests '*IT*' # Testcontainers IT（需 Docker）
./gradlew build                                       # 全门禁（共享基建变更）
./gradlew :archforge-server-admin:bootJar && \
  DB_PASSWORD=<容器密码> java --enable-preview -jar \
  archforge-server-admin/build/libs/archforge-server-admin.jar --spring.profiles.active=dev
```
