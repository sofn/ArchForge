-- V25: Remove example-task menus from the product database.
-- V8 inserted sys_menu 70–77 and granted them to role 2. example-task is no longer
-- assembled into server-admin; keep V8 for Flyway history and compensate here.
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 70 AND 77;
DELETE FROM sys_menu WHERE menu_id BETWEEN 70 AND 77;
SELECT setval('sys_menu_menu_id_seq', (SELECT COALESCE(MAX(menu_id), 1) FROM sys_menu));
