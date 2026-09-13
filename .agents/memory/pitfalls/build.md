# 踩坑：构建 / Gradle / 启动

## bootRun 复用 UP-TO-DATE 标记跳过重打包

**发现日期**: 2026-09-13

**问题描述**:
改完代码跑 `./gradlew bootRun`，任务秒回 UP-TO-DATE —— 实际是旧进程还在跑，Gradle 认为任务"已完成"直接复用。

**解决方案**:
改代码后验证走 `./gradlew :archforge-server-admin:bootJar` + 直接 `java -jar build/libs/archforge-server-admin.jar`，不要依赖 bootRun 的任务状态。bootRun 只用于首次启动场景。

## `--enable-preview` 位置必须在 `-jar` 之前

**发现日期**: 2026-09-13

**问题描述**:
`java -jar app.jar --enable-preview` → `UnsupportedClassVersionError: Preview features are not enabled`（class file version 69.65535）。

**根因**:
`-jar` 之后的参数全是应用参数传给 main()，JVM 旗标必须在 `-jar` 之前。

**解决方案**:
`java --enable-preview -jar archforge-server-admin.jar --spring.profiles.active=dev` —— JVM flags 在前，应用参数在后。

## `pkill -f` 匹配发起者自身命令行

**发现日期**: 2026-09-13

**问题描述**:
`pkill -f "archforge-server-admin.jar"` 把正在执行该命令的 shell 一并杀死（pattern 出现在自己的 cmdline 里），表现为命令 exit -1 且无输出。

**解决方案**:
- 用 `pgrep -f "^java.*archforge-server-admin"` 锚定进程名前缀
- 或先 `pgrep` 拿 PID 再 `kill <pid>`
- 脚本里永远假设 `-f` 会匹配自己

## 运行中 jar 被覆盖 → ClassNotFoundException

**发现日期**: 2026-09-13

**问题描述**:
`bootJar` 重建覆盖了正在运行的 jar，旧进程随后炸 `ClassNotFoundException`（Hibernate 内部类）。

**根因**:
JVM 对 jar 做 mmap/懒加载类，运行中覆盖 jar 文件导致后续类加载读到不一致内容。

**解决方案**:
改 jar 前先停旧进程；验证类迭代用 bootJar+重启而不是指望运行中进程热更。

## dev profile 数据源密码来自 DB_PASSWORD env

**发现日期**: 2026-09-13

**问题描述**:
`java -jar` 裸启动报 `SCRAM-based authentication, but no password was provided` —— `application-dev.yaml` 里 `password: ${DB_PASSWORD:}` 默认空。

**解决方案**:
`DB_PASSWORD=<容器 POSTGRES_PASSWORD> java --enable-preview -jar ...`。容器密码在 `docker inspect docker-postgres-1` 的 env 里（当前 `vFBzH8WuvgLSy7BQb5M7`）。
