-- V6: 删除 sys_user 表中的 post_id 字段 (移除岗位管理功能)

-- 删除 sys_user 表中的 post_id 字段
ALTER TABLE sys_user DROP COLUMN IF EXISTS post_id;
