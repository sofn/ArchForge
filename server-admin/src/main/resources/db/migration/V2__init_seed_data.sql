-- V2: 初始化基础数据

-- 默认用户 (密码: admin123)
INSERT INTO sys_user (user_id, post_id, role_id, dept_id, username, nickname, user_type, email, phone_number, sex, avatar, password, status, login_ip, login_date, is_admin, creator_id, create_time, remark, deleted)
VALUES (1, 1, 1, 103, 'admin', 'Admin', 0, 'admin@appforge.com', '15888888883', 0, '', '$2a$10$o55UFZAtyWnDpRV6dvQe8.c/MjlFacC49ASj2usNXm9BY74SYI/uG', 1, '127.0.0.1', NOW(), 1, null, NOW(), '管理员', 0);

-- 默认角色
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, status, creator_id, create_time, remark, deleted)
VALUES (1, '超级管理员', 'admin', 1, 1, 1, null, NOW(), '超级管理员拥有最高权限', 0),
       (2, '普通角色', 'common', 2, 2, 1, null, NOW(), '普通角色', 0);

-- 默认部门
INSERT INTO sys_dept (dept_id, parent_id, name, principal, phone, email, sort, status, type, remark, creator_id, create_time, deleted)
VALUES (100, 0, '总公司', '张三', '15888888888', 'admin@company.com', 0, 1, 1, '总公司', 1, NOW(), 0),
       (101, 100, '研发部门', '李四', '15888888888', 'rd@company.com', 1, 1, 3, '研发部门', 1, NOW(), 0),
       (102, 100, '市场部门', '王五', '15888888888', 'mk@company.com', 2, 1, 3, '市场部门', 1, NOW(), 0);

-- 默认参数
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, remark, creator_id, create_time, deleted)
VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 1, '蓝色 skin-blue、绿色 skin-green', 1, NOW(), 0),
       (2, '用户管理-账号初始密码', 'sys.user.initPassword', 'admin123', 1, '初始化密码', 1, NOW(), 0),
       (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 1, '深色/浅色主题', 1, NOW(), 0),
       (4, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 1, '是否开启注册', 1, NOW(), 0);

-- 默认通知
INSERT INTO sys_notice (notice_id, notice_title, notice_type, notice_content, status, remark, creator_id, create_time, deleted)
VALUES (1, '系统升级通知', 1, '系统将进行升级维护，届时暂停服务。', 1, '', 1, NOW(), 0),
       (2, '放假安排公告', 2, '请查看最新放假安排通知。', 1, '', 1, NOW(), 0);
