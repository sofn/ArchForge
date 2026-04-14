-- 参数设置 seed data
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, remark, creator_id, create_time, deleted) VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 1, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow', 1, '2022-05-21 08:30:54', 0);
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, remark, creator_id, create_time, deleted) VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', 'admin123', 1, '初始化密码', 1, '2022-05-21 08:30:54', 0);
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, remark, creator_id, create_time, deleted) VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 1, '深色主题theme-dark，浅色主题theme-light', 1, '2022-05-21 08:30:54', 0);
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, remark, creator_id, create_time, deleted) VALUES (4, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 1, '是否开启注册用户功能（true开启，false关闭）', 1, '2022-05-21 08:30:54', 0);

-- 通知公告 seed data
INSERT INTO sys_notice (notice_id, notice_title, notice_type, notice_content, status, remark, creator_id, create_time, deleted) VALUES (1, '系统升级通知', 1, '系统将于2025年1月1日进行升级维护，届时系统将暂停服务2小时。', 1, '', 1, '2022-05-21 08:30:54', 0);
INSERT INTO sys_notice (notice_id, notice_title, notice_type, notice_content, status, remark, creator_id, create_time, deleted) VALUES (2, '关于2025年春节放假安排的公告', 2, '根据国务院办公厅通知，2025年春节放假安排如下：1月28日至2月3日放假调休，共7天。', 1, '', 1, '2022-05-21 08:30:54', 0);

-- 操作日志 seed data (use "system_name" as Hibernate maps systemName to system_name)
INSERT INTO sys_oper_log (oper_id, username, module, summary, ip, address, os_name, browser, status, operating_time, creator_id, create_time, deleted) VALUES (1, 'admin', '系统管理', '菜单管理-添加菜单', '127.0.0.1', '内网IP', 'Linux', 'Chrome', 1, '2025-01-01 10:00:00', 1, '2025-01-01 10:00:00', 0);
INSERT INTO sys_oper_log (oper_id, username, module, summary, ip, address, os_name, browser, status, operating_time, creator_id, create_time, deleted) VALUES (2, 'admin', '系统管理', '用户管理-修改用户', '127.0.0.1', '内网IP', 'Linux', 'Chrome', 1, '2025-01-02 14:30:00', 1, '2025-01-02 14:30:00', 0);
INSERT INTO sys_oper_log (oper_id, username, module, summary, ip, address, os_name, browser, status, operating_time, creator_id, create_time, deleted) VALUES (3, 'ag1', '在线用户', '列表分页查询', '192.168.1.100', '内网IP', 'Windows', 'Firefox', 0, '2025-01-03 09:15:00', 2, '2025-01-03 09:15:00', 0);

-- 登录日志 seed data
INSERT INTO sys_login_log (info_id, username, ip, address, os_name, browser, status, behavior, login_time, creator_id, create_time, deleted) VALUES (1, 'admin', '127.0.0.1', '内网IP', 'Linux', 'Chrome', 1, '账号登录', '2025-01-01 08:00:00', 1, '2025-01-01 08:00:00', 0);
INSERT INTO sys_login_log (info_id, username, ip, address, os_name, browser, status, behavior, login_time, creator_id, create_time, deleted) VALUES (2, 'ag1', '192.168.1.100', '内网IP', 'Windows', 'Firefox', 0, '账号登录', '2025-01-02 09:00:00', 2, '2025-01-02 09:00:00', 0);
