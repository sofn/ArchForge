package com.lesofn.archforge.meta.table.internal.schema;

import static com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode.META_COLUMN_CODE_INVALID;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.internal.ddl.ColumnTypeResolver;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

        // 5. INDEX CHANGES
        changes.addAll(computeIndexChanges(oldColumns, newColumns));

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

        boolean typeChanged = !Objects.equals(oldColumn.getDataType(), newColumn.getDataType()) ||
                !Objects.equals(oldColumn.getLength(), newColumn.getLength()) ||
                !Objects.equals(oldColumn.getPrecision(), newColumn.getPrecision()) ||
                !Objects.equals(oldColumn.getScale(), newColumn.getScale()) ||
                !Objects.equals(oldColumn.getArrayElementType(), newColumn.getArrayElementType());

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

        return changes;
    }

    private List<SchemaChange> computeIndexChanges(List<MetaColumn> oldColumns, List<MetaColumn> newColumns) {
        Map<String, IndexDef> oldIndexes = buildIndexMap(oldColumns);
        Map<String, IndexDef> newIndexes = buildIndexMap(newColumns);

        List<SchemaChange> changes = new ArrayList<>();

        for (Map.Entry<String, IndexDef> entry : oldIndexes.entrySet()) {
            String name = entry.getKey();
            IndexDef oldDef = entry.getValue();
            IndexDef newDef = newIndexes.get(name);
            if (newDef == null) {
                changes.add(buildIndexChange(oldDef, null));
            } else if (!oldDef.equals(newDef)) {
                changes.add(buildIndexChange(oldDef, newDef));
            }
        }

        for (Map.Entry<String, IndexDef> entry : newIndexes.entrySet()) {
            String name = entry.getKey();
            if (!oldIndexes.containsKey(name)) {
                changes.add(buildIndexChange(null, entry.getValue()));
            }
        }

        return changes;
    }

    private Map<String, IndexDef> buildIndexMap(List<MetaColumn> columns) {
        Map<String, List<MetaColumn>> grouped = new HashMap<>();
        for (MetaColumn column : columns) {
            if (!isIndexEnabled(column)) {
                continue;
            }
            String group = column.getIndexGroup();
            if (group != null && !group.isEmpty()) {
                grouped.computeIfAbsent(group, k -> new ArrayList<>()).add(column);
            } else {
                grouped.computeIfAbsent(column.getColumnCode(), k -> new ArrayList<>()).add(column);
            }
        }

        Map<String, IndexDef> result = new HashMap<>();
        for (Map.Entry<String, List<MetaColumn>> entry : grouped.entrySet()) {
            List<MetaColumn> members = new ArrayList<>(entry.getValue());
            members.sort(Comparator.comparingInt(c -> c.getSort() == null ? 0 : c.getSort()));
            boolean unique = members.stream().anyMatch(c -> Boolean.TRUE.equals(c.getUnique()));
            String indexType = members.get(0).getIndexType();
            if (indexType == null || indexType.isEmpty()) {
                indexType = "BTREE";
            }
            result.put(entry.getKey(), new IndexDef(entry.getKey(), members, unique, indexType));
        }
        return result;
    }

    private boolean isIndexEnabled(MetaColumn column) {
        return Boolean.TRUE.equals(column.getUnique()) || Boolean.TRUE.equals(column.getIndex());
    }

    private SchemaChange buildIndexChange(IndexDef oldDef, IndexDef newDef) {
        SchemaChange change = new SchemaChange();
        change.setType(SchemaChangeType.ALTER_INDEX);
        if (oldDef != null) {
            MetaColumn representative = oldDef.members.get(0);
            change.setOldColumn(representative);
            change.setOldUnique(representative.getUnique());
            change.setOldIndex(representative.getIndex());
            change.setOldIndexType(oldDef.indexType);
            change.setOldIndexGroup(oldDef.name);
            change.setOldGroupColumns(oldDef.members);
        }
        if (newDef != null) {
            MetaColumn representative = newDef.members.get(0);
            change.setNewColumn(representative);
            change.setNewUnique(representative.getUnique());
            change.setNewIndex(representative.getIndex());
            change.setNewIndexType(newDef.indexType);
            change.setNewIndexGroup(newDef.name);
            change.setNewGroupColumns(newDef.members);
        }
        return change;
    }

    private record IndexDef(String name, List<MetaColumn> members, boolean unique, String indexType) {
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof IndexDef that))
                return false;
            return unique == that.unique &&
                    Objects.equals(name, that.name) &&
                    Objects.equals(indexType, that.indexType) &&
                    memberCodes().equals(that.memberCodes());
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, unique, indexType, memberCodes());
        }

        private List<String> memberCodes() {
            return members.stream().map(MetaColumn::getColumnCode).toList();
        }
    }
}
