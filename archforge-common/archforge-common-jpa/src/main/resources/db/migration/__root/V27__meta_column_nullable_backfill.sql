-- nullable 现在是物理 DDL 真源（required 仅表单校验）。
-- 历史上 nullable 是死字段：请求默认 true + @DynamicInsert 省略 null 后 DB DEFAULT 1 兜底，
-- 存量行的 nullable=1 均为默认值噪音，而 required=1 的列物理上已是 NOT NULL。
-- 把 required 的历史 NOT NULL 意图一次性转移给 nullable，消除模型与物理漂移。
UPDATE sys_meta_table_column
SET nullable = 0
WHERE is_required = 1
  AND deleted = 0;
