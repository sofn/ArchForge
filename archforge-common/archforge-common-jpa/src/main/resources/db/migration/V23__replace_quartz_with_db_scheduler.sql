-- V23: Replace the Quartz scheduler with db-scheduler.
--
-- Branches (both are idempotent, so fresh and legacy databases converge to the same state):
--   * fresh database: V4.1/V5 were deleted from the migration history, so this migration
--     creates sys_scheduled_job / sys_job_log / scheduled_tasks from scratch and seeds the
--     demo job + menu.
--   * legacy database (Quartz era): drops the eleven qrtz_* tables, moves sys_quartz_job /
--     sys_quartz_log rows into the new tables (quartz-style `?` normalized to `*`), drops the
--     old tables, updates the menu remark, and removes the stale V4.1/V5 rows from
--     flyway_schema_history so `validate` no longer misses them.
--
-- API/permission contract is unchanged: REST path stays `/quartz`, permission codes stay
-- `monitor:job:*`, the menu row keeps menu_id=100 and path `/system/quartz/index`.

-- ====================================================================
-- 1. db-scheduler runtime table (upstream PostgreSQL DDL, priority enabled)
-- ====================================================================
CREATE TABLE IF NOT EXISTS scheduled_tasks (
  task_name text not null,
  task_instance text not null,
  task_data bytea,
  execution_time timestamp with time zone not null,
  picked BOOLEAN not null,
  picked_by text,
  last_success timestamp with time zone,
  last_failure timestamp with time zone,
  consecutive_failures INT,
  last_heartbeat timestamp with time zone,
  version BIGINT not null,
  priority SMALLINT,
  PRIMARY KEY (task_name, task_instance)
);
CREATE INDEX IF NOT EXISTS execution_time_idx ON scheduled_tasks (execution_time);
CREATE INDEX IF NOT EXISTS last_heartbeat_idx ON scheduled_tasks (last_heartbeat);
CREATE INDEX IF NOT EXISTS priority_execution_time_idx ON scheduled_tasks (priority desc, execution_time asc);

-- ====================================================================
-- 2. Job metadata + execution log (replaces sys_quartz_job / sys_quartz_log)
-- ====================================================================
CREATE TABLE IF NOT EXISTS sys_scheduled_job (
    id              BIGSERIAL    PRIMARY KEY,
    job_name        VARCHAR(100) NOT NULL,
    job_group       VARCHAR(100) NOT NULL DEFAULT 'DEFAULT',
    description     VARCHAR(255),
    bean_name       VARCHAR(200) NOT NULL,
    method_name     VARCHAR(100) NOT NULL,
    method_params   TEXT,
    cron            VARCHAR(100) NOT NULL,
    misfire_policy  SMALLINT,
    concurrent      INT,
    status          SMALLINT,
    creator_id      BIGINT,
    create_time     TIMESTAMP,
    updater_id      BIGINT,
    update_time     TIMESTAMP,
    deleted         INT          NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_scheduled_job_name_group UNIQUE (job_name, job_group)
);
CREATE INDEX IF NOT EXISTS idx_sys_scheduled_job_group ON sys_scheduled_job (job_group);

CREATE TABLE IF NOT EXISTS sys_job_log (
    id            BIGSERIAL   PRIMARY KEY,
    job_id        BIGINT,
    job_name      VARCHAR(100),
    job_group     VARCHAR(100),
    bean_name     VARCHAR(200),
    method_name   VARCHAR(100),
    method_params TEXT,
    status        SMALLINT,
    error_message TEXT,
    duration_ms   BIGINT,
    started_at    TIMESTAMP,
    finished_at   TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_sys_job_log_job_id ON sys_job_log (job_id);
CREATE INDEX IF NOT EXISTS idx_sys_job_log_started_at ON sys_job_log (started_at);

-- ====================================================================
-- 3. Legacy migration: move sys_quartz_* rows, then drop the Quartz tables
-- ====================================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'sys_quartz_job') THEN
        -- Quartz-style `?` (day-of-month/day-of-week) is normalized to `*`
        INSERT INTO sys_scheduled_job
            (id, job_name, job_group, description, bean_name, method_name, method_params, cron,
             misfire_policy, concurrent, status, creator_id, create_time, updater_id, update_time, deleted)
        SELECT id, job_name, job_group, description, bean_name, method_name, method_params,
               replace(cron, '?', '*'), misfire_policy, concurrent, status,
               creator_id, create_time, updater_id, update_time, deleted
        FROM sys_quartz_job
        ON CONFLICT (id) DO NOTHING;

        -- keep the sequence ahead of migrated ids
        PERFORM setval(pg_get_serial_sequence('sys_scheduled_job', 'id'),
                       (SELECT COALESCE(MAX(id), 1) FROM sys_scheduled_job));

        -- the seeded demo bean was renamed with the framework swap
        UPDATE sys_scheduled_job SET bean_name = 'demoSchedulerJob' WHERE bean_name = 'demoQuartzJob';
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'sys_quartz_log') THEN
        INSERT INTO sys_job_log
            (id, job_id, job_name, job_group, bean_name, method_name, method_params,
             status, error_message, duration_ms, started_at, finished_at)
        SELECT id, job_id, job_name, job_group, bean_name, method_name, method_params,
               status, error_message, duration_ms, started_at, finished_at
        FROM sys_quartz_log
        ON CONFLICT (id) DO NOTHING;

        PERFORM setval(pg_get_serial_sequence('sys_job_log', 'id'),
                       (SELECT COALESCE(MAX(id), 1) FROM sys_job_log));
    END IF;
END $$;

DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS; -- trigger
DROP TABLE IF EXISTS QRTZ_SIMPROP_TRIGGERS; -- trigger
DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS; -- trigger
DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS; -- trigger
DROP TABLE IF EXISTS QRTZ_FIRED_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_PAUSED_TRIGGER_GRPS;
DROP TABLE IF EXISTS QRTZ_SCHEDULER_STATE;
DROP TABLE IF EXISTS QRTZ_LOCKS;
DROP TABLE IF EXISTS QRTZ_CALENDARS;
DROP TABLE IF EXISTS QRTZ_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_JOB_DETAILS;

DROP TABLE IF EXISTS sys_quartz_job;
DROP TABLE IF EXISTS sys_quartz_log;

-- Prune the deleted V4.1/V5 rows from flyway history on legacy databases
-- (they were removed from the repository; ignore-migration-patterns tolerates the gap).
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'flyway_schema_history') THEN
        DELETE FROM flyway_schema_history WHERE version IN ('4.1', '5');
    END IF;
END $$;

-- ====================================================================
-- 4. Seed: demo job + menu (fresh databases; legacy rows win via ON CONFLICT)
-- ====================================================================
INSERT INTO sys_scheduled_job
    (job_name, job_group, description, bean_name, method_name, method_params, cron, misfire_policy, concurrent, status, deleted, create_time, update_time)
VALUES
    ('demo-hello', 'DEFAULT', 'Demo: prints hello every 30s', 'demoSchedulerJob', 'helloWorld', NULL, '0/30 * * * * *', 1, 0, 1, 0, NOW(), NOW())
ON CONFLICT (job_name, job_group) DO NOTHING;

INSERT INTO sys_menu
    (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted)
VALUES
    (100, '定时任务', 1, 'SystemQuartz', 1, '/system/quartz/index', 0, 'system:quartz:list',
     '{"title":"定时任务","icon":"ep:timer","showParent":true}',
     1, 'db-scheduler 反射调度任务管理', 0, NOW(), 1, NOW(), 0)
ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 100) ON CONFLICT DO NOTHING;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 100) ON CONFLICT DO NOTHING;

-- Legacy menu rows: de-brand the description only (path/permission stay for frontend compat).
UPDATE sys_menu SET remark = 'db-scheduler 反射调度任务管理'
WHERE menu_id = 100 AND remark LIKE '%Quartz%';
