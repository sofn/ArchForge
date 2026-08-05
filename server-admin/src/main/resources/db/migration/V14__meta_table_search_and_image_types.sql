-- V13: 元表格搜索方式与图片类型支持

-- 字段配置增加搜索方式
ALTER TABLE sys_meta_table_column
    ADD COLUMN search_type VARCHAR(16) DEFAULT 'EXACT';

COMMENT ON COLUMN sys_meta_table_column.search_type IS '搜索方式：EXACT|LIKE|RANGE';

-- 更新 data_type 检查约束，支持 IMAGE、MULTI_IMAGE
ALTER TABLE sys_meta_table_column
    DROP CONSTRAINT IF EXISTS sys_meta_table_column_data_type_check;

ALTER TABLE sys_meta_table_column
    ADD CONSTRAINT sys_meta_table_column_data_type_check
        CHECK (data_type::text = ANY (ARRAY[
            'STRING', 'TEXT', 'INTEGER', 'DECIMAL', 'BOOLEAN', 'DATE', 'DATETIME',
            'ENUM', 'JSON', 'FILE', 'IMAGE', 'MULTI_IMAGE', 'UUID', 'TIMESTAMPTZ', 'ARRAY', 'GEO'
        ]::text[]));
