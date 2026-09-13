# ArchForge 踩坑索引

> 只放目录和写入规则。**不要把整本踩坑当每次会话必读材料。**
> 先读本索引，再按当前任务打开对应分类文件。

## 怎么用

- 新会话：读本文件标题列表，定位相关分类即可，不要通读分类全文。
- 改认证 / sa-token / 权限：`pitfalls/auth.md`
- 改构建 / Gradle / 启动：`pitfalls/build.md`
- 改 Flyway / 数据源 / PG：`pitfalls/db-flyway.md`
- 改 meta-table / 内省 / 导入：`pitfalls/meta-table.md`
- 其它后端框架问题：`pitfalls/backend.md`（待建）

## 写入规则

只记录真实故障、根因和规避方式。编码规范不要写到这里，写到根目录 `AGENTS.md` 或 `docs/specs/`。

格式：

```markdown
## 问题标题

**发现日期**: YYYY-MM-DD

**问题描述**:
现象。

**根因**:
为什么。

**解决方案**:
怎么避免、怎么修。
```

## 分类目录

### [认证 / sa-token / 权限](pitfalls/auth.md)（3）

- sa-token `*:*:*` 通配只按段字面匹配，单段权限串不命中
- `LoginContext` 在 sa-token context 未初始化时抛 `SaTokenContextException`
- 请求日志 uid 需经 token 直解，不能依赖请求线程上下文

### [构建 / Gradle / 启动](pitfalls/build.md)（4）

- `bootRun` 复用 UP-TO-DATE 标记会跳过重打包，改 jar 用 `bootJar` + 直接 `java -jar`
- `--enable-preview` 是 JVM 旗标必须在 `-jar` 之前，放后面变应用参数
- `pkill -f` 模式会匹配发起它的 shell 自身命令行导致自杀
- 运行中的 jar 被 `bootJar` 覆盖会出现 ClassNotFound 诡异故障，重启即恢复

### [Flyway / 数据源 / PG](pitfalls/db-flyway.md)（2）

- 本地删除迁移脚本后 flyway_schema_history 残留导致 validate 失败，需清理 history 行
- 容器 PG 的 SCRAM verifier 与 docker-compose env 脱节时 ALTER USER 对齐

### [meta-table / 内省 / 导入](pitfalls/meta-table.md)（2）

- `*Mapper` 类名撞 ArchUnit `noMapperNamedClasses` 规则，内省组件用 `*Mapping` 命名
- 平台表黑名单必须与 `validateTableCode` 命名规则解耦（meta_ 是自家前缀不被拦）
