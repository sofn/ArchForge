-- V12: 更新 sys_meta_table_column 的 data_type 检查约束，支持增强数据类型

ALTER TABLE sys_meta_table_column
    DROP CONSTRAINT IF EXISTS sys_meta_table_column_data_type_check;

ALTER TABLE sys_meta_table_column
    ADD CONSTRAINT sys_meta_table_column_data_type_check
        CHECK (data_type::text = ANY (ARRAY[
            'STRING', 'TEXT', 'INTEGER', 'DECIMAL', 'BOOLEAN', 'DATE', 'DATETIME',
            'ENUM', 'JSON', 'FILE', 'UUID', 'TIMESTAMPTZ', 'ARRAY', 'GEO'
        ]::text[]));
