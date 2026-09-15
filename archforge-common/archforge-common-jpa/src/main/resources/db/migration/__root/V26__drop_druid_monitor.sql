-- Druid pool/monitor removed from the stack — drop its console menu entry and grants.
DELETE FROM sys_role_menu WHERE menu_id = 14;
DELETE FROM sys_menu WHERE menu_id = 14;
