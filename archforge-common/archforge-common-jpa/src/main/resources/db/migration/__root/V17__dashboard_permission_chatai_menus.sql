-- Welcome / ChatAI / permission matrix menus
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (122, '仪表盘', 1, 'Welcome', 0, '/welcome', 0, 'dashboard:view', '{"title":"仪表盘","icon":"ep:home-filled","rank":0}', 1, '', 0, CURRENT_TIMESTAMP, null, null, 0) ON CONFLICT (menu_id) DO NOTHING;
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (123, 'ChatAI', 1, 'ChatAi', 0, '/chatai/index', 0, 'chatai:use', '{"title":"ChatAI","icon":"ri:chat-search-line","rank":9}', 1, '', 0, CURRENT_TIMESTAMP, null, null, 0) ON CONFLICT (menu_id) DO NOTHING;
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (124, '权限矩阵', 1, 'PermissionPage', 1, '/permission/page/index', 0, 'system:role:query', '{"title":"权限矩阵"}', 1, '', 0, CURRENT_TIMESTAMP, null, null, 0) ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 122) ON CONFLICT DO NOTHING;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 123) ON CONFLICT DO NOTHING;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 124) ON CONFLICT DO NOTHING;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 122) ON CONFLICT DO NOTHING;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 123) ON CONFLICT DO NOTHING;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 124) ON CONFLICT DO NOTHING;

SELECT setval('sys_menu_menu_id_seq', (SELECT COALESCE(MAX(menu_id), 0) FROM sys_menu));
