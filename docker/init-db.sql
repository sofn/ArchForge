-- Create application databases. CREATE DATABASE cannot run inside a function.
SELECT 'CREATE DATABASE archforge'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'archforge')\gexec
