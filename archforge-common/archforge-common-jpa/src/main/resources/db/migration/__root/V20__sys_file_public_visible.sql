-- V20: 文件可见性模型
-- sys_file 增加 public_visible 列：仅标记为公开的文件允许匿名通过 /web/file/{id} 访问，
-- 其余文件要求登录且为上传者本人。历史数据均为图片上传（上传时已强制图片类型），
-- 统一回填为公开以保持既有文章封面/正文图片的匿名渲染能力。
-- 注意：本项目布尔列统一使用 INT(0/1)，与 sys_file.deleted、sys_user.is_admin 保持一致。

ALTER TABLE sys_file ADD COLUMN IF NOT EXISTS public_visible INT NOT NULL DEFAULT 0;

UPDATE sys_file SET public_visible = 1 WHERE deleted = 0 AND content_type LIKE 'image/%';
