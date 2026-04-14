-- V1: 初始化表结构

-- 用户信息表
CREATE TABLE IF NOT EXISTS sys_user
(
    user_id      BIGINT AUTO_INCREMENT COMMENT '用户ID' PRIMARY KEY,
    post_id      BIGINT                  NULL COMMENT '职位id',
    role_id      BIGINT                  NULL COMMENT '角色id',
    dept_id      BIGINT                  NULL COMMENT '部门ID',
    username     VARCHAR(64)             NOT NULL COMMENT '用户账号',
    nickname     VARCHAR(32)             NOT NULL COMMENT '用户昵称',
    user_type    SMALLINT     DEFAULT 0  NULL COMMENT '用户类型（00系统用户）',
    email        VARCHAR(128) DEFAULT '' NULL COMMENT '用户邮箱',
    phone_number VARCHAR(18)  DEFAULT '' NULL COMMENT '手机号码',
    sex          SMALLINT     DEFAULT 0  NULL COMMENT '用户性别（0男 1女 2未知）',
    avatar       VARCHAR(512) DEFAULT '' NULL COMMENT '头像地址',
    password     VARCHAR(128) DEFAULT '' NOT NULL COMMENT '密码',
    status       SMALLINT     DEFAULT 0  NOT NULL COMMENT '帐号状态（1正常 2停用 3冻结）',
    login_ip     VARCHAR(128) DEFAULT '' NULL COMMENT '最后登录IP',
    login_date   DATETIME                NULL COMMENT '最后登录时间',
    is_admin     TINYINT(1)   DEFAULT 0  NOT NULL COMMENT '超级管理员标志（1是，0否）',
    creator_id   BIGINT                  NULL COMMENT '创建者ID',
    create_time  DATETIME                NULL COMMENT '创建时间',
    updater_id   BIGINT                  NULL COMMENT '更新者ID',
    update_time  DATETIME                NULL COMMENT '更新时间',
    remark       VARCHAR(512)            NULL COMMENT '备注',
    deleted      TINYINT(1)   DEFAULT 0  NOT NULL COMMENT '删除标志（0代表存在 1代表删除）'
) COMMENT '用户信息表';

-- 菜单权限表
CREATE TABLE IF NOT EXISTS sys_menu
(
    menu_id     BIGINT AUTO_INCREMENT COMMENT '菜单ID' PRIMARY KEY,
    menu_name   VARCHAR(64)                NOT NULL COMMENT '菜单名称',
    menu_type   SMALLINT      DEFAULT 0    NOT NULL COMMENT '菜单类型',
    router_name VARCHAR(255)  DEFAULT ''   NOT NULL COMMENT '路由名称',
    parent_id   BIGINT        DEFAULT 0    NOT NULL COMMENT '父菜单ID',
    path        VARCHAR(255)               NULL COMMENT '组件路径',
    is_button   TINYINT(1)    DEFAULT 0    NOT NULL COMMENT '是否按钮',
    permission  VARCHAR(128)               NULL COMMENT '权限标识',
    meta_info   VARCHAR(1024) DEFAULT '{}' NOT NULL COMMENT '路由元信息',
    status      SMALLINT      DEFAULT 0    NOT NULL COMMENT '菜单状态（1启用 0停用）',
    remark      VARCHAR(256)  DEFAULT ''   NULL COMMENT '备注',
    creator_id  BIGINT                     NULL COMMENT '创建者ID',
    create_time DATETIME                   NULL COMMENT '创建时间',
    updater_id  BIGINT                     NULL COMMENT '更新者ID',
    update_time DATETIME                   NULL COMMENT '更新时间',
    deleted     TINYINT(1)    DEFAULT 0    NOT NULL COMMENT '逻辑删除'
) COMMENT '菜单权限表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role
(
    role_id     BIGINT AUTO_INCREMENT COMMENT '角色ID' PRIMARY KEY,
    role_name   VARCHAR(32)             NOT NULL COMMENT '角色名称',
    role_key    VARCHAR(128)            NOT NULL COMMENT '角色标识',
    role_sort   INT                     NOT NULL COMMENT '显示顺序',
    data_scope  SMALLINT     DEFAULT 1  NULL COMMENT '数据范围',
    dept_id_set VARCHAR(1024)           NULL COMMENT '部门ID集合',
    status      SMALLINT     DEFAULT 1  NOT NULL COMMENT '状态（1正常 0停用）',
    creator_id  BIGINT                  NULL COMMENT '创建者ID',
    create_time DATETIME                NULL COMMENT '创建时间',
    updater_id  BIGINT                  NULL COMMENT '更新者ID',
    update_time DATETIME                NULL COMMENT '更新时间',
    remark      VARCHAR(512)            NULL COMMENT '备注',
    deleted     TINYINT(1)   DEFAULT 0  NOT NULL COMMENT '逻辑删除'
) COMMENT '角色表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu
(
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
) COMMENT '角色菜单关联表';

-- 部门表
CREATE TABLE IF NOT EXISTS sys_dept
(
    dept_id     BIGINT AUTO_INCREMENT COMMENT '部门ID' PRIMARY KEY,
    parent_id   BIGINT        DEFAULT 0    NOT NULL COMMENT '父部门ID',
    name        VARCHAR(64)                NOT NULL COMMENT '部门名称',
    principal   VARCHAR(64)   DEFAULT ''   NULL COMMENT '负责人',
    phone       VARCHAR(18)   DEFAULT ''   NULL COMMENT '联系电话',
    email       VARCHAR(128)  DEFAULT ''   NULL COMMENT '邮箱',
    sort        INT           DEFAULT 0    NOT NULL COMMENT '显示顺序',
    status      SMALLINT      DEFAULT 1    NOT NULL COMMENT '状态（1正常 0停用）',
    type        SMALLINT      DEFAULT 3    NOT NULL COMMENT '类型（1公司 2分公司 3部门）',
    remark      VARCHAR(512)               NULL COMMENT '备注',
    creator_id  BIGINT                     NULL COMMENT '创建者ID',
    create_time DATETIME                   NULL COMMENT '创建时间',
    updater_id  BIGINT                     NULL COMMENT '更新者ID',
    update_time DATETIME                   NULL COMMENT '更新时间',
    deleted     TINYINT(1)    DEFAULT 0    NOT NULL COMMENT '逻辑删除'
) COMMENT '部门表';

-- 参数配置表
CREATE TABLE IF NOT EXISTS sys_config
(
    config_id    BIGINT AUTO_INCREMENT COMMENT '参数ID' PRIMARY KEY,
    config_name  VARCHAR(128)  DEFAULT '' NULL COMMENT '参数名称',
    config_key   VARCHAR(128)  DEFAULT '' NULL COMMENT '参数键名',
    config_value VARCHAR(512)  DEFAULT '' NULL COMMENT '参数键值',
    config_type  INT           DEFAULT 0  NOT NULL COMMENT '系统内置（1是 0否）',
    remark       VARCHAR(512)             NULL COMMENT '备注',
    creator_id   BIGINT                   NULL COMMENT '创建者ID',
    create_time  DATETIME                 NULL COMMENT '创建时间',
    updater_id   BIGINT                   NULL COMMENT '更新者ID',
    update_time  DATETIME                 NULL COMMENT '更新时间',
    deleted      TINYINT(1)    DEFAULT 0  NOT NULL COMMENT '逻辑删除'
) COMMENT '参数配置表';

-- 通知公告表
CREATE TABLE IF NOT EXISTS sys_notice
(
    notice_id      BIGINT AUTO_INCREMENT COMMENT '公告ID' PRIMARY KEY,
    notice_title   VARCHAR(128)  DEFAULT '' NULL COMMENT '公告标题',
    notice_type    INT           DEFAULT 1  NOT NULL COMMENT '公告类型（1通知 2公告）',
    notice_content TEXT                     NULL COMMENT '公告内容',
    status         INT           DEFAULT 1  NOT NULL COMMENT '状态（1正常 0关闭）',
    remark         VARCHAR(512)             NULL COMMENT '备注',
    creator_id     BIGINT                   NULL COMMENT '创建者ID',
    create_time    DATETIME                 NULL COMMENT '创建时间',
    updater_id     BIGINT                   NULL COMMENT '更新者ID',
    update_time    DATETIME                 NULL COMMENT '更新时间',
    deleted        TINYINT(1)    DEFAULT 0  NOT NULL COMMENT '逻辑删除'
) COMMENT '通知公告表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_oper_log
(
    oper_id        BIGINT AUTO_INCREMENT COMMENT '操作ID' PRIMARY KEY,
    username       VARCHAR(64)   DEFAULT '' NULL COMMENT '操作人员',
    module         VARCHAR(64)   DEFAULT '' NULL COMMENT '所属模块',
    summary        VARCHAR(256)  DEFAULT '' NULL COMMENT '操作概要',
    ip             VARCHAR(128)  DEFAULT '' NULL COMMENT '操作IP',
    address        VARCHAR(256)  DEFAULT '' NULL COMMENT '操作地点',
    os_name        VARCHAR(64)   DEFAULT '' NULL COMMENT '操作系统',
    browser        VARCHAR(64)   DEFAULT '' NULL COMMENT '浏览器',
    status         INT           DEFAULT 1  NOT NULL COMMENT '操作状态（1成功 0失败）',
    operating_time DATETIME                 NULL COMMENT '操作时间',
    creator_id     BIGINT                   NULL COMMENT '创建者ID',
    create_time    DATETIME                 NULL COMMENT '创建时间',
    updater_id     BIGINT                   NULL COMMENT '更新者ID',
    update_time    DATETIME                 NULL COMMENT '更新时间',
    deleted        TINYINT(1)    DEFAULT 0  NOT NULL COMMENT '逻辑删除'
) COMMENT '操作日志表';

-- 登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log
(
    info_id     BIGINT AUTO_INCREMENT COMMENT '登录ID' PRIMARY KEY,
    username    VARCHAR(64)   DEFAULT '' NULL COMMENT '用户名',
    ip          VARCHAR(128)  DEFAULT '' NULL COMMENT '登录IP',
    address     VARCHAR(256)  DEFAULT '' NULL COMMENT '登录地点',
    os_name     VARCHAR(64)   DEFAULT '' NULL COMMENT '操作系统',
    browser     VARCHAR(64)   DEFAULT '' NULL COMMENT '浏览器',
    status      INT           DEFAULT 1  NOT NULL COMMENT '登录状态（1成功 0失败）',
    behavior    VARCHAR(128)  DEFAULT '' NULL COMMENT '登录行为',
    login_time  DATETIME                 NULL COMMENT '登录时间',
    creator_id  BIGINT                   NULL COMMENT '创建者ID',
    create_time DATETIME                 NULL COMMENT '创建时间',
    updater_id  BIGINT                   NULL COMMENT '更新者ID',
    update_time DATETIME                 NULL COMMENT '更新时间',
    deleted     TINYINT(1)    DEFAULT 0  NOT NULL COMMENT '逻辑删除'
) COMMENT '登录日志表';
