-- V5: Quartz seed data
--   - One paused demo job targeting demoQuartzJob#helloWorld every 30s
--   - System -> 定时任务 menu entry under the existing System menu (parent menu_id=1)

INSERT INTO sys_quartz_job
    (job_name, job_group, description, bean_name, method_name, method_params, cron, misfire_policy, concurrent, status)
VALUES
    ('demo-hello', 'DEFAULT', 'Demo: prints hello every 30s', 'demoQuartzJob', 'helloWorld', NULL, '0/30 * * * * ?', 1, FALSE, 1);

-- Menu entry. menu_id=100 picked above the highest seeded id (65) to avoid future-collision risk.
INSERT INTO sys_menu
    (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted)
VALUES
    (100, '定时任务', 1, 'SystemQuartz', 1, '/system/quartz/index', 0, 'system:quartz:list',
     '{"title":"定时任务","icon":"ep:timer","showParent":true}',
     1, 'Quartz 反射调度任务管理', 0, NOW(), 1, NOW(), 0)
ON CONFLICT (menu_id) DO NOTHING;

-- Grant both seeded roles access to the new menu.
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 100) ON CONFLICT DO NOTHING;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 100) ON CONFLICT DO NOTHING;
