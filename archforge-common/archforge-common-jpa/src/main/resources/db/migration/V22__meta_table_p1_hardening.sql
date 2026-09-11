-- V22: 元表格模块 P1 加固
-- 1) sys_meta_table 增加乐观锁版本号：并发编辑表定义时以 @Version 检测丢失更新
-- 2) uq_meta_table_column 全局唯一约束改为部分唯一索引（排除软删行）：
--    软删字段行仍占用 (table_id, column_code)，全局约束导致"删列→再加同名字段"必死锁

-- 3) data_type 检查约束补 REFERENCE：V14 漏掉该类型导致引用字段元数据无法落库
ALTER TABLE sys_meta_table_column
    DROP CONSTRAINT IF EXISTS sys_meta_table_column_data_type_check;

ALTER TABLE sys_meta_table_column
    ADD CONSTRAINT sys_meta_table_column_data_type_check
        CHECK (data_type::text = ANY (ARRAY[
            'STRING', 'TEXT', 'INTEGER', 'DECIMAL', 'BOOLEAN', 'DATE', 'DATETIME',
            'ENUM', 'JSON', 'FILE', 'IMAGE', 'MULTI_IMAGE', 'UUID', 'TIMESTAMPTZ', 'ARRAY', 'GEO',
            'REFERENCE'
        ]::text[]));

ALTER TABLE sys_meta_table ADD COLUMN version INT NOT NULL DEFAULT 0;

ALTER TABLE sys_meta_table_column DROP CONSTRAINT uq_meta_table_column;

CREATE UNIQUE INDEX IF NOT EXISTS uq_meta_table_column
    ON sys_meta_table_column (table_id, column_code)
    WHERE deleted = 0;
