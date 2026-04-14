-- V1: 初始化表结构 (PostgreSQL)

-- 用户信息表
CREATE TABLE IF NOT EXISTS sys_user
(
    user_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    post_id      BIGINT,
    role_id      BIGINT,
    dept_id      BIGINT,
    username     VARCHAR(64)             NOT NULL,
    nickname     VARCHAR(32)             NOT NULL,
    user_type    SMALLINT     DEFAULT 0,
    email        VARCHAR(128) DEFAULT '',
    phone_number VARCHAR(18)  DEFAULT '',
    sex          SMALLINT     DEFAULT 0,
    avatar       VARCHAR(512) DEFAULT '',
    password     VARCHAR(128) DEFAULT '' NOT NULL,
    status       SMALLINT     DEFAULT 0  NOT NULL,
    login_ip     VARCHAR(128) DEFAULT '',
    login_date   TIMESTAMP,
    is_admin     BOOLEAN      DEFAULT FALSE NOT NULL,
    creator_id   BIGINT,
    create_time  TIMESTAMP,
    updater_id   BIGINT,
    update_time  TIMESTAMP,
    remark       VARCHAR(512),
    deleted      BOOLEAN      DEFAULT FALSE NOT NULL
);

-- 菜单权限表
CREATE TABLE IF NOT EXISTS sys_menu
(
    menu_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    menu_name   VARCHAR(64)                NOT NULL,
    menu_type   SMALLINT      DEFAULT 0    NOT NULL,
    router_name VARCHAR(255)  DEFAULT ''   NOT NULL,
    parent_id   BIGINT        DEFAULT 0    NOT NULL,
    path        VARCHAR(255),
    is_button   BOOLEAN       DEFAULT FALSE NOT NULL,
    permission  VARCHAR(128),
    meta_info   VARCHAR(1024) DEFAULT '{}' NOT NULL,
    status      SMALLINT      DEFAULT 0    NOT NULL,
    remark      VARCHAR(256)  DEFAULT '',
    creator_id  BIGINT,
    create_time TIMESTAMP,
    updater_id  BIGINT,
    update_time TIMESTAMP,
    deleted     BOOLEAN       DEFAULT FALSE NOT NULL
);

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role
(
    role_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_name   VARCHAR(32)             NOT NULL,
    role_key    VARCHAR(128)            NOT NULL,
    role_sort   INT                     NOT NULL,
    data_scope  SMALLINT     DEFAULT 1,
    dept_id_set VARCHAR(1024),
    status      SMALLINT     DEFAULT 1  NOT NULL,
    creator_id  BIGINT,
    create_time TIMESTAMP,
    updater_id  BIGINT,
    update_time TIMESTAMP,
    remark      VARCHAR(512),
    deleted     BOOLEAN      DEFAULT FALSE NOT NULL
);

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu
(
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

-- 部门表
CREATE TABLE IF NOT EXISTS sys_dept
(
    dept_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parent_id   BIGINT        DEFAULT 0    NOT NULL,
    name        VARCHAR(64)                NOT NULL,
    principal   VARCHAR(64)   DEFAULT '',
    phone       VARCHAR(18)   DEFAULT '',
    email       VARCHAR(128)  DEFAULT '',
    sort        INT           DEFAULT 0    NOT NULL,
    status      SMALLINT      DEFAULT 1    NOT NULL,
    type        SMALLINT      DEFAULT 3    NOT NULL,
    remark      VARCHAR(512),
    creator_id  BIGINT,
    create_time TIMESTAMP,
    updater_id  BIGINT,
    update_time TIMESTAMP,
    deleted     BOOLEAN       DEFAULT FALSE NOT NULL
);

-- 参数配置表
CREATE TABLE IF NOT EXISTS sys_config
(
    config_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    config_name  VARCHAR(128)  DEFAULT '',
    config_key   VARCHAR(128)  DEFAULT '',
    config_value VARCHAR(512)  DEFAULT '',
    config_type  INT           DEFAULT 0  NOT NULL,
    remark       VARCHAR(512),
    creator_id   BIGINT,
    create_time  TIMESTAMP,
    updater_id   BIGINT,
    update_time  TIMESTAMP,
    deleted      BOOLEAN       DEFAULT FALSE NOT NULL
);

-- 通知公告表
CREATE TABLE IF NOT EXISTS sys_notice
(
    notice_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    notice_title   VARCHAR(128)  DEFAULT '',
    notice_type    INT           DEFAULT 1  NOT NULL,
    notice_content TEXT,
    status         INT           DEFAULT 1  NOT NULL,
    remark         VARCHAR(512),
    creator_id     BIGINT,
    create_time    TIMESTAMP,
    updater_id     BIGINT,
    update_time    TIMESTAMP,
    deleted        BOOLEAN       DEFAULT FALSE NOT NULL
);

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_oper_log
(
    oper_id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username       VARCHAR(64)   DEFAULT '',
    module         VARCHAR(64)   DEFAULT '',
    summary        VARCHAR(256)  DEFAULT '',
    ip             VARCHAR(128)  DEFAULT '',
    address        VARCHAR(256)  DEFAULT '',
    os_name        VARCHAR(64)   DEFAULT '',
    browser        VARCHAR(64)   DEFAULT '',
    status         INT           DEFAULT 1  NOT NULL,
    operating_time TIMESTAMP,
    creator_id     BIGINT,
    create_time    TIMESTAMP,
    updater_id     BIGINT,
    update_time    TIMESTAMP,
    deleted        BOOLEAN       DEFAULT FALSE NOT NULL
);

-- 登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log
(
    info_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username    VARCHAR(64)   DEFAULT '',
    ip          VARCHAR(128)  DEFAULT '',
    address     VARCHAR(256)  DEFAULT '',
    os_name     VARCHAR(64)   DEFAULT '',
    browser     VARCHAR(64)   DEFAULT '',
    status      INT           DEFAULT 1  NOT NULL,
    behavior    VARCHAR(128)  DEFAULT '',
    login_time  TIMESTAMP,
    creator_id  BIGINT,
    create_time TIMESTAMP,
    updater_id  BIGINT,
    update_time TIMESTAMP,
    deleted     BOOLEAN       DEFAULT FALSE NOT NULL
);
