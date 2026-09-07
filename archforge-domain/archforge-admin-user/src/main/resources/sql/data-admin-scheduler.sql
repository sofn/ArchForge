-- Admin scheduled-job seed data (db-scheduler runtime).
-- Dev/staging: Flyway is off, so this script also creates db-scheduler's own table
-- (in Flyway-managed environments V23 owns the DDL).

-- db-scheduler runtime table (upstream PostgreSQL DDL, priority enabled)
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

-- Demo job: bean 'demoSchedulerJob', no-arg 'helloWorld', every 30 seconds.
-- Misfire policy / concurrent are kept for UI compatibility; the db-scheduler
-- runtime treats missed runs as skipped and executions as serial.
INSERT INTO sys_scheduled_job
    (job_name, job_group, description, bean_name, method_name, method_params, cron, misfire_policy, concurrent, status, deleted, create_time, update_time)
VALUES
    ('demo-hello', 'DEFAULT', 'Demo: prints hello every 30s', 'demoSchedulerJob', 'helloWorld', NULL, '0/30 * * * * *', 1, 0, 1, 0, NOW(), NOW())
ON CONFLICT (job_name, job_group) DO NOTHING;

-- Menu entry (menu_id=100 stays stable; path targets the admin frontend component
-- views/system/quartz/index.vue, router/permission codes unchanged).
INSERT INTO sys_menu
    (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted)
VALUES
    (100, '定时任务', 1, 'SystemQuartz', 1, '/system/quartz/index', 0, 'system:quartz:list',
     '{"title":"定时任务","icon":"ep:timer","showParent":true}',
     1, 'db-scheduler 反射调度任务管理', 0, NOW(), 1, NOW(), 0)
ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 100) ON CONFLICT DO NOTHING;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 100) ON CONFLICT DO NOTHING;
