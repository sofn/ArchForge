create table sys_dept (
    dept_id     bigint auto_increment comment '部门ID' primary key,
    parent_id   bigint        default 0    not null comment '父部门ID',
    name        varchar(64)                not null comment '部门名称',
    principal   varchar(64)   default ''   null comment '负责人',
    phone       varchar(18)   default ''   null comment '联系电话',
    email       varchar(128)  default ''   null comment '邮箱',
    sort        int           default 0    not null comment '显示顺序',
    status      smallint      default 1    not null comment '状态（1正常 0停用）',
    type        smallint      default 3    not null comment '类型（1公司 2分公司 3部门）',
    remark      varchar(512)               null comment '备注',
    creator_id  bigint                     null comment '创建者ID',
    create_time datetime                   null comment '创建时间',
    updater_id  bigint                     null comment '更新者ID',
    update_time datetime                   null comment '更新时间',
    deleted     tinyint(1)    default 0    not null comment '逻辑删除'
) comment '部门表';

INSERT INTO sys_dept (dept_id, parent_id, name, principal, phone, email, sort, status, type, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (100, 0, '杭州总公司', '张三', '15888888888', 'admin@company.com', 0, 1, 1, '总公司', 1, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_dept (dept_id, parent_id, name, principal, phone, email, sort, status, type, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (101, 100, '郑州分公司', '李四', '15888888888', 'zz@company.com', 1, 1, 2, '郑州分公司', 1, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_dept (dept_id, parent_id, name, principal, phone, email, sort, status, type, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (102, 100, '深圳分公司', '王五', '15888888888', 'sz@company.com', 2, 1, 2, '深圳分公司', 1, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_dept (dept_id, parent_id, name, principal, phone, email, sort, status, type, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (103, 101, '研发部门', '赵六', '15888888888', 'rd@company.com', 1, 1, 3, '研发部门', 1, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_dept (dept_id, parent_id, name, principal, phone, email, sort, status, type, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (104, 101, '市场部门', '钱七', '15888888888', 'mk@company.com', 2, 1, 3, '市场部门', 1, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_dept (dept_id, parent_id, name, principal, phone, email, sort, status, type, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (105, 101, '测试部门', '孙八', '15888888888', 'qa@company.com', 3, 0, 3, '测试部门', 1, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_dept (dept_id, parent_id, name, principal, phone, email, sort, status, type, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (106, 101, '财务部门', '周九', '15888888888', 'fin@company.com', 4, 1, 3, '财务部门', 1, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_dept (dept_id, parent_id, name, principal, phone, email, sort, status, type, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (107, 101, '运维部门', '吴十', '15888888888', 'ops@company.com', 5, 0, 3, '运维部门', 1, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_dept (dept_id, parent_id, name, principal, phone, email, sort, status, type, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (108, 102, '市场部门', '郑十一', '15888888888', 'mk2@company.com', 1, 1, 3, '深圳市场部门', 1, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_dept (dept_id, parent_id, name, principal, phone, email, sort, status, type, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (109, 102, '财务部门', '冯十二', '15888888888', 'fin2@company.com', 2, 1, 3, '深圳财务部门', 1, '2022-05-21 08:30:54', null, null, 0);
