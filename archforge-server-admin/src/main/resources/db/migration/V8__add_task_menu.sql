-- V8: 添加任务管理示例菜单

INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (70, '任务管理', 1, 'ExampleTask', 0, '/example/task/index', 0, 'example:task:list', '{"title":"任务管理","icon":"ep:list","showParent":true,"rank":4}', 1, '任务管理示例菜单', 1, '2022-05-21 08:30:54', 1, '2022-05-21 08:30:54', 0);

INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (71, '任务查询', 0, ' ', 70, '', 1, 'example:task:query', '{"title":"任务查询"}', 1, '', 1, '2022-05-21 08:30:54', 1, '2022-05-21 08:30:54', 0);

INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (72, '任务新增', 0, ' ', 70, '', 1, 'example:task:add', '{"title":"任务新增"}', 1, '', 1, '2022-05-21 08:30:54', 1, '2022-05-21 08:30:54', 0);

INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (73, '任务修改', 0, ' ', 70, '', 1, 'example:task:edit', '{"title":"任务修改"}', 1, '', 1, '2022-05-21 08:30:54', 1, '2022-05-21 08:30:54', 0);

INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (74, '任务删除', 0, ' ', 70, '', 1, 'example:task:remove', '{"title":"任务删除"}', 1, '', 1, '2022-05-21 08:30:54', 1, '2022-05-21 08:30:54', 0);

INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (75, '任务开始', 0, ' ', 70, '', 1, 'example:task:start', '{"title":"任务开始"}', 1, '', 1, '2022-05-21 08:30:54', 1, '2022-05-21 08:30:54', 0);

INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (76, '任务完成', 0, ' ', 70, '', 1, 'example:task:complete', '{"title":"任务完成"}', 1, '', 1, '2022-05-21 08:30:54', 1, '2022-05-21 08:30:54', 0);

INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (77, '任务取消', 0, ' ', 70, '', 1, 'example:task:cancel', '{"title":"任务取消"}', 1, '', 1, '2022-05-21 08:30:54', 1, '2022-05-21 08:30:54', 0);

-- 任务管理权限赋给普通角色
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 70);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 71);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 72);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 73);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 74);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 75);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 76);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 77);

-- 重置序列
SELECT setval('sys_menu_menu_id_seq', (SELECT COALESCE(MAX(menu_id), 0) FROM sys_menu));
