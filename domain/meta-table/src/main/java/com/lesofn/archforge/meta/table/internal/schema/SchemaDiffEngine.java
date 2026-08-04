package com.lesofn.archforge.meta.table.internal.schema;

import static com.lesofn.archforge.meta.table.internal.exception.MetaTableErrorCode.META_COLUMN_CODE_INVALID;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.internal.ddl.ColumnTypeResolver;
import com.lesofn.archforge.meta.table.internal.exception.MetaTableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 元表格 Schema 差异计算引擎。
 */
@Component
@RequiredArgsConstructor
public class SchemaDiffEngine {

    private final ColumnTypeResolver columnTypeResolver;

    /**
     * 比较旧字段列表与新字段列表，生成有序的 Schema 变更列表。
     *
     * <p>
     * 执行顺序：RENAME → DROP → ADD → ALTER（type/default/null/index）。
     */
    public List<SchemaChange> diff(MetaTable table, List<MetaColumn> oldColumns, List<MetaColumn> newColumns) {
        validate(table, oldColumns, newColumns);

        Map<Long, MetaColumn> oldById = new HashMap<>();
        for (MetaColumn old : oldColumns) {
            if (old.getId() != null) {
                oldById.put(old.getId(), old);
            }
        }

        Map<Long, MetaColumn> newById = new HashMap<>();
        for (MetaColumn next : newColumns) {
            if (next.getId() != null) {
                newById.put(next.getId(), next);
            }
        }

        List<SchemaChange> changes = new ArrayList<>();

        // 1. RENAME
        for (MetaColumn next : newColumns) {
            if (next.getId() == null) {
                continue;
            }
            MetaColumn old = oldById.get(next.getId());
            if (old != null && !Objects.equals(old.getColumnCode(), next.getColumnCode())) {
                changes.add(rename(old, next));
            }
        }

        // 2. DROP
        for (MetaColumn old : oldColumns) {
            if (old.getId() != null && !newById.containsKey(old.getId())) {
                changes.add(drop(old));
            }
        }

        // 3. ADD
        for (MetaColumn next : newColumns) {
            if (next.getId() == null || !oldById.containsKey(next.getId())) {
                changes.add(add(next));
            }
        }

        // 4. ALTER
        for (MetaColumn next : newColumns) {
            if (next.getId() == null) {
                continue;
            }
            MetaColumn old = oldById.get(next.getId());
            if (old != null) {
                changes.addAll(alter(old, next));
            }
        }

        return changes;
    }

    private void validate(MetaTable table, List<MetaColumn> oldColumns, List<MetaColumn> newColumns) {
        if (newColumns == null || newColumns.isEmpty()) {
            throw new MetaTableException("Schema 演进至少需要保留一个字段");
        }

        Set<String> newCodes = new HashSet<>();
        Map<Long, MetaColumn> oldById = new HashMap<>();
        for (MetaColumn old : oldColumns) {
            if (old.getId() != null) {
                oldById.put(old.getId(), old);
            }
        }

        for (MetaColumn next : newColumns) {
            if (next.getColumnCode() == null || next.getColumnCode().isEmpty()) {
                throw new MetaTableException(META_COLUMN_CODE_INVALID, "字段编码不能为空");
            }
            if (!newCodes.add(next.getColumnCode())) {
                throw new MetaTableException(META_COLUMN_CODE_INVALID, "字段编码重复: " + next.getColumnCode());
            }
            if (next.getId() != null && !oldById.containsKey(next.getId())) {
                throw new MetaTableException(META_COLUMN_CODE_INVALID, "字段 id 不存在: " + next.getId() + "，请清空新增字段的 id");
            }
        }
    }

    private SchemaChange add(MetaColumn column) {
        SchemaChange change = new SchemaChange();
        change.setType(SchemaChangeType.ADD_COLUMN);
        change.setNewColumn(column);
        return change;
    }

    private SchemaChange drop(MetaColumn column) {
        SchemaChange change = new SchemaChange();
        change.setType(SchemaChangeType.DROP_COLUMN);
        change.setOldColumn(column);
        return change;
    }

    private SchemaChange rename(MetaColumn oldColumn, MetaColumn newColumn) {
        SchemaChange change = new SchemaChange();
        change.setType(SchemaChangeType.RENAME_COLUMN);
        change.setOldColumn(oldColumn);
        change.setNewColumn(newColumn);
        return change;
    }

    private List<SchemaChange> alter(MetaColumn oldColumn, MetaColumn newColumn) {
        List<SchemaChange> changes = new ArrayList<>();

        boolean typeChanged = !Objects.equals(oldColumn.getDataType(), newColumn.getDataType()) || !Objects.equals(oldColumn
                .getLength(), newColumn.getLength()) || !Objects.equals(oldColumn.getPrecision(), newColumn.getPrecision()) ||
                !Objects.equals(oldColumn.getScale(), newColumn.getScale());

        if (typeChanged) {
            SchemaChange change = new SchemaChange();
            change.setType(SchemaChangeType.ALTER_TYPE);
            change.setOldColumn(oldColumn);
            change.setNewColumn(newColumn);
            change.setOldType(columnTypeResolver.resolve(oldColumn));
            change.setNewType(columnTypeResolver.resolve(newColumn));
            changes.add(change);
        }

        boolean defaultChanged = !Objects.equals(oldColumn.getDefaultValue(), newColumn.getDefaultValue());
        if (defaultChanged) {
            SchemaChange change = new SchemaChange();
            change.setType(SchemaChangeType.ALTER_DEFAULT);
            change.setOldColumn(oldColumn);
            change.setNewColumn(newColumn);
            change.setOldDefault(columnTypeResolver.formatDefaultValue(oldColumn));
            change.setNewDefault(columnTypeResolver.formatDefaultValue(newColumn));
            changes.add(change);
        }

        boolean nullChanged = !Objects.equals(oldColumn.getRequired(), newColumn.getRequired());
        if (nullChanged) {
            SchemaChange change = new SchemaChange();
            change.setType(SchemaChangeType.ALTER_NULL);
            change.setOldColumn(oldColumn);
            change.setNewColumn(newColumn);
            change.setOldRequired(oldColumn.getRequired());
            change.setNewRequired(newColumn.getRequired());
            changes.add(change);
        }

        boolean indexChanged = !Objects.equals(oldColumn.getUnique(), newColumn.getUnique()) || !Objects.equals(oldColumn
                .getIndex(), newColumn.getIndex());
        if (indexChanged) {
            SchemaChange change = new SchemaChange();
            change.setType(SchemaChangeType.ALTER_INDEX);
            change.setOldColumn(oldColumn);
            change.setNewColumn(newColumn);
            change.setOldUnique(oldColumn.getUnique());
            change.setNewUnique(newColumn.getUnique());
            change.setOldIndex(oldColumn.getIndex());
            change.setNewIndex(newColumn.getIndex());
            changes.add(change);
        }

        return changes;
    }
}
