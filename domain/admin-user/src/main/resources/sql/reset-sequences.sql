-- Reset PostgreSQL identity sequences to the maximum id after seed imports
SELECT setval('sys_user_user_id_seq', COALESCE((SELECT MAX(user_id) FROM sys_user), 1));
SELECT setval('sys_menu_menu_id_seq', COALESCE((SELECT MAX(menu_id) FROM sys_menu), 1));
SELECT setval('sys_role_role_id_seq', COALESCE((SELECT MAX(role_id) FROM sys_role), 1));
SELECT setval('sys_dept_dept_id_seq', COALESCE((SELECT MAX(dept_id) FROM sys_dept), 1));
SELECT setval('sys_config_config_id_seq', COALESCE((SELECT MAX(config_id) FROM sys_config), 1));
SELECT setval('sys_notice_notice_id_seq', COALESCE((SELECT MAX(notice_id) FROM sys_notice), 1));
SELECT setval('sys_oper_log_oper_id_seq', COALESCE((SELECT MAX(oper_id) FROM sys_oper_log), 1));
SELECT setval('sys_login_log_info_id_seq', COALESCE((SELECT MAX(info_id) FROM sys_login_log), 1));
SELECT setval('sys_file_file_id_seq', COALESCE((SELECT MAX(file_id) FROM sys_file), 1));
SELECT setval('blog_category_id_seq', COALESCE((SELECT MAX(id) FROM blog_category), 1));
SELECT setval('blog_article_id_seq', COALESCE((SELECT MAX(id) FROM blog_article), 1));
