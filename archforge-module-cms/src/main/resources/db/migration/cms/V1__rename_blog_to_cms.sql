-- cms/V1: blog → cms rename (module is now archforge-module-cms). Idempotent — each
-- statement only fires when the old object exists. Fresh databases reach the
-- same state via V13 (creates blog_*) + this rename; databases that already ran
-- V13 get their data carried over instead of re-created.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables
               WHERE table_schema = 'public' AND table_name = 'blog_category')
       AND NOT EXISTS (SELECT 1 FROM information_schema.tables
               WHERE table_schema = 'public' AND table_name = 'cms_category') THEN
        ALTER TABLE blog_category RENAME TO cms_category;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.tables
               WHERE table_schema = 'public' AND table_name = 'blog_article')
       AND NOT EXISTS (SELECT 1 FROM information_schema.tables
               WHERE table_schema = 'public' AND table_name = 'cms_article') THEN
        ALTER TABLE blog_article RENAME TO cms_article;
    END IF;

    -- BIGSERIAL sequences keep their old names after ALTER TABLE RENAME; align them.
    IF EXISTS (SELECT 1 FROM pg_class WHERE relname = 'blog_category_id_seq' AND relkind = 'S')
       AND NOT EXISTS (SELECT 1 FROM pg_class WHERE relname = 'cms_category_id_seq' AND relkind = 'S') THEN
        ALTER SEQUENCE blog_category_id_seq RENAME TO cms_category_id_seq;
    END IF;
    IF EXISTS (SELECT 1 FROM pg_class WHERE relname = 'blog_article_id_seq' AND relkind = 'S')
       AND NOT EXISTS (SELECT 1 FROM pg_class WHERE relname = 'cms_article_id_seq' AND relkind = 'S') THEN
        ALTER SEQUENCE blog_article_id_seq RENAME TO cms_article_id_seq;
    END IF;
END $$;

-- permissions / menus: blog:* → cms:*, /blog → /cms, router names Blog* → Cms*
UPDATE sys_menu SET permission = replace(permission, 'blog:', 'cms:')
    WHERE permission LIKE 'blog:%';
UPDATE sys_menu SET path = replace(path, '/blog', '/cms')
    WHERE path LIKE '/blog%';
UPDATE sys_menu SET router_name = replace(router_name, 'Blog', 'Cms')
    WHERE router_name LIKE 'Blog%';
UPDATE sys_menu SET menu_name = 'CMS管理', meta_info = replace(meta_info, '博客管理', 'CMS管理')
    WHERE menu_name = '博客管理';
