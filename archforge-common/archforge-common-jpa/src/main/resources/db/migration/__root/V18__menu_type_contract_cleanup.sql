-- Buttons are is_button=1, not a menu_type value.
-- Canonical menu_type: 1=page, 2=catalog, 3=iframe, 4=outside link.
UPDATE sys_menu SET is_button = 1 WHERE menu_type = 0 AND deleted = 0;
UPDATE sys_menu SET menu_type = 1 WHERE is_button = 1 AND (menu_type IS NULL OR menu_type NOT IN (1, 2, 3, 4));
