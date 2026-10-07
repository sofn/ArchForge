-- V28: 定时任务的按钮权限。
-- 控制器以前校验 monitor:job:*，可没有任何菜单/按钮授予这些权限码——除超级管理员（*）外，
-- 拿到「定时任务」菜单的角色连列表都是 403，角色编辑里也无从勾选。控制器改用菜单已在用的
-- system:scheduler-job:* 命名空间；这里补齐新增/修改/删除三个按钮（幂等）。
-- 只授予管理员角色：定时任务可以反射调用任意 Bean 方法，写权限默认不下放给其他角色。

-- 种子脚本用显式 menu_id 插过行，序列可能落后于 MAX(menu_id)
SELECT setval('sys_menu_menu_id_seq', (SELECT COALESCE(MAX(menu_id), 0) FROM sys_menu));

INSERT INTO sys_menu (menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status,
                      remark, creator_id, create_time, updater_id, update_time, deleted)
SELECT b.menu_name, 1, ' ', 100, '', 1, b.permission, '{"title":"' || b.menu_name || '"}', 1,
       '', 1, NOW(), 1, NOW(), 0
FROM (VALUES ('定时任务新增', 'system:scheduler-job:add'),
             ('定时任务修改', 'system:scheduler-job:edit'),
             ('定时任务删除', 'system:scheduler-job:remove')) AS b (menu_name, permission)
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu m WHERE m.parent_id = 100 AND m.permission = b.permission AND m.deleted = 0
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id
FROM sys_menu
WHERE parent_id = 100
  AND deleted = 0
  AND permission IN ('system:scheduler-job:add', 'system:scheduler-job:edit', 'system:scheduler-job:remove')
ON CONFLICT DO NOTHING;
