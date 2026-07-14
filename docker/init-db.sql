-- 创建应用所需的数据库（幂等）
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'archforge_user') THEN
        CREATE DATABASE archforge_user;
    END IF;
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'archforge_task') THEN
        CREATE DATABASE archforge_task;
    END IF;
END $$;
