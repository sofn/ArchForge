-- Simulates a Quartz-era database right before V23: qrtz_* tables (PostgreSQL folds
-- unquoted identifiers to lowercase, so that's how Quartz's DDL actually lands),
-- sys_quartz_job/sys_quartz_log with data, and the sys_menu/sys_role_menu rows a
-- real V1-V22 database would already have. flyway_schema_history is intentionally
-- left for Flyway's baseline to create.

CREATE TABLE qrtz_job_details (
  sched_name varchar(120) NOT NULL,
  job_name varchar(200) NOT NULL,
  job_group varchar(200) NOT NULL,
  PRIMARY KEY (sched_name, job_name, job_group)
);
INSERT INTO qrtz_job_details VALUES ('ArchForgeScheduler', 'legacy-job', 'DEFAULT');

CREATE TABLE qrtz_triggers (
  sched_name varchar(120) NOT NULL,
  trigger_name varchar(200) NOT NULL,
  trigger_group varchar(200) NOT NULL,
  trigger_state varchar(16) NOT NULL,
  next_fire_time bigint,
  PRIMARY KEY (sched_name, trigger_name, trigger_group)
);
INSERT INTO qrtz_triggers VALUES ('ArchForgeScheduler', 'legacy-job', 'DEFAULT', 'WAITING', 0);

CREATE TABLE sys_quartz_job (
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
  deleted         INT          NOT NULL DEFAULT 0
);
INSERT INTO sys_quartz_job
  (id, job_name, job_group, description, bean_name, method_name, method_params, cron,
   misfire_policy, concurrent, status, create_time, update_time, deleted)
VALUES
  (42, 'legacy-job', 'DEFAULT', 'Legacy quartz job', 'demoQuartzJob', 'printTime', '["x"]',
   '0/15 * * * * ?', 1, 0, 0, NOW(), NOW(), 0);

CREATE TABLE sys_quartz_log (
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
INSERT INTO sys_quartz_log
  (job_id, job_name, job_group, bean_name, method_name, method_params, status, duration_ms, started_at, finished_at)
VALUES
  (42, 'legacy-job', 'DEFAULT', 'demoQuartzJob', 'printTime', '["x"]', 0, 12, NOW(), NOW());

CREATE TABLE sys_menu (
  menu_id    BIGSERIAL PRIMARY KEY,
  menu_name  VARCHAR(100),
  menu_type  INT,
  router_name VARCHAR(100),
  parent_id  BIGINT,
  path       VARCHAR(255),
  is_button  INT,
  permission VARCHAR(255),
  meta_info  TEXT,
  status     SMALLINT,
  remark     VARCHAR(255),
  creator_id BIGINT,
  create_time TIMESTAMP,
  updater_id BIGINT,
  update_time TIMESTAMP,
  deleted    INT NOT NULL DEFAULT 0
);
INSERT INTO sys_menu
  (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted)
VALUES
  (100, '定时任务', 1, 'SystemQuartz', 1, '/system/quartz/index', 0, 'system:quartz:list',
   '{"title":"定时任务","icon":"ep:timer","showParent":true}', 1, 'Quartz 反射调度任务管理', 0, NOW(), 1, NOW(), 0);

CREATE TABLE sys_role_menu (
  role_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, menu_id)
);
INSERT INTO sys_role_menu VALUES (1, 100), (2, 100);
