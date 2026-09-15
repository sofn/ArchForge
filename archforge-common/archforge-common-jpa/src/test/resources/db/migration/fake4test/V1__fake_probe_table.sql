-- 测试夹具：为 FlywayModuleOrchestrator 的发现/双历史表行为提供一个模块本地迁移。
-- 本文件只存在于 common-jpa 的 test classpath，不进生产 jar；H2 兼容且幂等（root 与模块
-- 两个 Flyway 实例都会执行同一目录）。
CREATE TABLE IF NOT EXISTS flyway_orch_probe (
    id INT PRIMARY KEY,
    note VARCHAR(32)
);
