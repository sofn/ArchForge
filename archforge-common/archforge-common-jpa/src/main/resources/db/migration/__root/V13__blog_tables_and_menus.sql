-- blog tables
CREATE TABLE IF NOT EXISTS blog_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    slug VARCHAR(128) NOT NULL UNIQUE,
    sort_order INT DEFAULT 0,
    status INT NOT NULL DEFAULT 1,
    creator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater_id BIGINT,
    update_time TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS blog_article (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    title VARCHAR(256) NOT NULL,
    slug VARCHAR(256) NOT NULL UNIQUE,
    summary VARCHAR(1024),
    content TEXT NOT NULL,
    cover_image_file_id BIGINT,
    author_id BIGINT,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    publish_time TIMESTAMP,
    creator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater_id BIGINT,
    update_time TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0
);

-- 菜单：博客管理（目录）
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (111, '博客管理', 2, 'BlogManagement', 0, '/blog', 0, 'blog:manage', '{"title":"博客管理","icon":"ri:article-line","showParent":true,"rank":4}', 1, '', 0, CURRENT_TIMESTAMP, null, null, 0) ON CONFLICT (menu_id) DO NOTHING;

-- 子菜单：目录管理
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (112, '目录管理', 1, 'BlogCategory', 111, '/blog/category/index', 0, 'blog:category:list', '{"title":"目录管理"}', 1, '', 0, CURRENT_TIMESTAMP, null, null, 0) ON CONFLICT (menu_id) DO NOTHING;

-- 子菜单：内容管理
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (113, '内容管理', 1, 'BlogArticle', 111, '/blog/article/index', 0, 'blog:article:list', '{"title":"内容管理"}', 1, '', 0, CURRENT_TIMESTAMP, null, null, 0) ON CONFLICT (menu_id) DO NOTHING;

-- 按钮权限
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (114, '新增目录', 0, ' ', 112, '', 1, 'blog:category:add', '{"title":"新增目录"}', 1, '', 0, CURRENT_TIMESTAMP, null, null, 0) ON CONFLICT (menu_id) DO NOTHING;
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (115, '编辑目录', 0, ' ', 112, '', 1, 'blog:category:edit', '{"title":"编辑目录"}', 1, '', 0, CURRENT_TIMESTAMP, null, null, 0) ON CONFLICT (menu_id) DO NOTHING;
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (116, '删除目录', 0, ' ', 112, '', 1, 'blog:category:remove', '{"title":"删除目录"}', 1, '', 0, CURRENT_TIMESTAMP, null, null, 0) ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (117, '新增文章', 0, ' ', 113, '', 1, 'blog:article:add', '{"title":"新增文章"}', 1, '', 0, CURRENT_TIMESTAMP, null, null, 0) ON CONFLICT (menu_id) DO NOTHING;
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (118, '编辑文章', 0, ' ', 113, '', 1, 'blog:article:edit', '{"title":"编辑文章"}', 1, '', 0, CURRENT_TIMESTAMP, null, null, 0) ON CONFLICT (menu_id) DO NOTHING;
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (119, '删除文章', 0, ' ', 113, '', 1, 'blog:article:remove', '{"title":"删除文章"}', 1, '', 0, CURRENT_TIMESTAMP, null, null, 0) ON CONFLICT (menu_id) DO NOTHING;
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (120, '发布文章', 0, ' ', 113, '', 1, 'blog:article:publish', '{"title":"发布文章"}', 1, '', 0, CURRENT_TIMESTAMP, null, null, 0) ON CONFLICT (menu_id) DO NOTHING;
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) OVERRIDING SYSTEM VALUE VALUES (121, '下线文章', 0, ' ', 113, '', 1, 'blog:article:offline', '{"title":"下线文章"}', 1, '', 0, CURRENT_TIMESTAMP, null, null, 0) ON CONFLICT (menu_id) DO NOTHING;

-- 将新菜单绑定到管理员角色（role_id=2）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 111) ON CONFLICT DO NOTHING;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 112) ON CONFLICT DO NOTHING;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 113) ON CONFLICT DO NOTHING;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 114) ON CONFLICT DO NOTHING;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 115) ON CONFLICT DO NOTHING;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 116) ON CONFLICT DO NOTHING;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 117) ON CONFLICT DO NOTHING;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 118) ON CONFLICT DO NOTHING;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 119) ON CONFLICT DO NOTHING;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 120) ON CONFLICT DO NOTHING;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 121) ON CONFLICT DO NOTHING;

-- 重置序列
SELECT setval('sys_menu_menu_id_seq', (SELECT COALESCE(MAX(menu_id), 0) FROM sys_menu));
