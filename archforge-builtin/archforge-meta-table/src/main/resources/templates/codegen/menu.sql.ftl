-- ${tableName}（${tableCode}）菜单：幂等（按 permission 去重），不硬编码 menu_id
-- 模块感知 Flyway：版本号在本目录 db/migration/${moduleSlug}/ 内自增
INSERT INTO sys_menu (menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info,
                      status, creator_id, create_time, deleted)
SELECT '${tableName}', 2, '${className}Management', 0, '/${tableCode}', 0, '${tableCode}:manage',
       '{"title":"${tableName}","icon":"ri:table-line","showParent":true,"rank":${moduleCode?c}}',
       1, 0, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = '${tableCode}:manage');

INSERT INTO sys_menu (menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info,
                      status, creator_id, create_time, deleted)
SELECT '${tableName}', 1, '${className}',
       (SELECT menu_id FROM sys_menu WHERE permission = '${tableCode}:manage'),
       '/${tableCode}/index', 0, '${tableCode}:list',
       '{"title":"${tableName}"}', 1, 0, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = '${tableCode}:list');

<#assign buttonDefs = [["add", "新增"], ["edit", "编辑"], ["remove", "删除"], ["export", "导出"], ["import", "导入"]]>
<#list buttonDefs as btn>
INSERT INTO sys_menu (menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info,
                      status, creator_id, create_time, deleted)
SELECT '${btn[1]}', 0, ' ',
       (SELECT menu_id FROM sys_menu WHERE permission = '${tableCode}:list'),
       '', 1, '${tableCode}:${btn[0]}',
       '{"title":"${btn[1]}"}', 1, 0, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = '${tableCode}:${btn[0]}');

</#list>
-- 绑定管理员角色（role_id=2），按 permission 前缀批量绑定
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu
WHERE permission LIKE '${tableCode}:%'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm
                  WHERE rm.role_id = 2 AND rm.menu_id = sys_menu.menu_id);
