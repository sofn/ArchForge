-- V11: 元表格增强数据类型与索引配置

ALTER TABLE sys_meta_table_column
    ADD COLUMN index_type VARCHAR(16) DEFAULT 'BTREE',
    ADD COLUMN index_group VARCHAR(64),
    ADD COLUMN array_element_type VARCHAR(32);

COMMENT ON COLUMN sys_meta_table_column.index_type IS '索引类型：BTREE|GIN|GIST|FULLTEXT';
COMMENT ON COLUMN sys_meta_table_column.index_group IS '相同 group 的列组成复合索引';
COMMENT ON COLUMN sys_meta_table_column.array_element_type IS 'ARRAY 元素类型：STRING|INTEGER|DECIMAL|BOOLEAN';
