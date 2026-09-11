-- V21: 统一元表格模块权限命名空间为连字符式
-- 菜单/按钮种子原先使用 meta:table:*（冒号式），而控制器 @SaCheckPermission 使用 meta-table:*（连字符式），
-- 导致按菜单授权的角色对全部写操作 403。统一为 meta-table:*。

-- 重命名已种子的 8 个权限（幂等：无匹配行时无操作）
UPDATE sys_menu
SET permission = 'meta-table:' || split_part(permission, ':', 3)
WHERE permission LIKE 'meta:table:%';

-- 补充缺失的“生成代码”按钮（幂等：按 parent+permission 去重，menu_id 冲突时跳过）
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted)
OVERRIDING SYSTEM VALUE
SELECT 93, '元表格生成代码', 0, ' ', 80, '', 1, 'meta-table:generate', '{"title":"元表格生成代码"}', 1, '', 1, '2022-05-21 08:30:54', 1, '2022-05-21 08:30:54', 0
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE parent_id = 80 AND permission = 'meta-table:generate' AND deleted = 0
)
ON CONFLICT (menu_id) DO NOTHING;

-- 把新按钮赋给管理员角色
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 93) ON CONFLICT DO NOTHING;

-- 重置序列
SELECT setval('sys_menu_menu_id_seq', (SELECT COALESCE(MAX(menu_id), 0) FROM sys_menu));
