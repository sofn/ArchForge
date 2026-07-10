-- V4: 修正菜单 path，使前端 addAsyncRoutes 能匹配到正确的 views 目录
UPDATE sys_menu SET path = '/monitor/logs' WHERE menu_id = 12;
UPDATE sys_menu SET path = '/monitor/online/index' WHERE menu_id = 13;
UPDATE sys_menu SET path = '/monitor/druid/index' WHERE menu_id = 14;
UPDATE sys_menu SET path = '/monitor/cache/index' WHERE menu_id = 16;
UPDATE sys_menu SET path = '/monitor/logs/operation/index' WHERE menu_id = 18;
UPDATE sys_menu SET path = '/monitor/logs/login/index' WHERE menu_id = 19;
