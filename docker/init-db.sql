-- Create application databases. CREATE DATABASE cannot run inside a function.
SELECT 'CREATE DATABASE archforge_user'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'archforge_user')\gexec
SELECT 'CREATE DATABASE archforge_task'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'archforge_task')\gexec
