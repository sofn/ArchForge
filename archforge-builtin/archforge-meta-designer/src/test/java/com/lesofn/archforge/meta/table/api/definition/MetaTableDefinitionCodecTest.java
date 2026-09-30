package com.lesofn.archforge.meta.table.api.definition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.domain.OptionItem;
import jakarta.persistence.Column;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MetaTableDefinitionCodecTest {

    @Test
    void roundTripPreservesAllFields() {
        MetaTable table = new MetaTable();
        table.setTableCode("article");
        table.setTableName("文章");
        table.setDescription("内容表");
        table.setTablePrefix("meta_");
        table.setStatus(1);
        table.setSchemaVersion(1);

        MetaColumn column = new MetaColumn();
        column.setColumnCode("title");
        column.setColumnName("标题");
        column.setDataType(MetaColumnType.ENUM);
        column.setLength(200);
        column.setPrecision(10);
        column.setScale(2);
        column.setNullable(false);
        column.setDefaultValue("draft");
        column.setUnique(true);
        column.setRequired(true);
        column.setSearchable(true);
        column.setListVisible(true);
        column.setIndex(true);
        column.setSort(3);
        column.setOptions(List.of(new OptionItem("草稿", "draft")));
        column.setReferenceTable("author");
        column.setReferenceColumn("id");
        column.setDisplayExpression("{name}");
        column.setTenantColumn(true);
        column.setOwnerColumn(true);
        column.setIndexType("btree");
        column.setIndexGroup("idx_title");
        column.setArrayElementType("text");
        column.setSearchType("like");
        column.setDictCode("article_status");

        TableDefinition definition = MetaTableDefinitionCodec.toDefinition(table, List.of(column));
        String yaml = MetaTableDefinitionCodec.toYaml(definition);
        TableDefinition parsed = MetaTableDefinitionCodec.fromYaml(yaml);

        assertEquals(definition, parsed);

        MetaTable back = MetaTableDefinitionCodec.toTableEntity(parsed);
        assertEquals("article", back.getTableCode());
        assertEquals("文章", back.getTableName());
        assertEquals("meta_article", back.physicalTableName());

        MetaColumn columnBack = MetaTableDefinitionCodec.toColumnEntities(parsed).get(0);
        assertEquals("title", columnBack.getColumnCode());
        assertEquals(MetaColumnType.ENUM, columnBack.getDataType());
        assertEquals(Integer.valueOf(200), columnBack.getLength());
        assertEquals(true, columnBack.getTenantColumn());
        assertEquals(List.of(new OptionItem("草稿", "draft")), columnBack.getOptions());
        assertEquals("article_status", columnBack.getDictCode());
    }

    @Test
    void minimalDefinitionParses() {
        String yaml = """
                tableCode: task
                tableName: 任务
                columns:
                  - columnCode: title
                    columnName: 标题
                    dataType: STRING
                """;
        TableDefinition def = MetaTableDefinitionCodec.fromYaml(yaml);
        assertEquals("task", def.getTableCode());
        assertEquals(1, def.getColumns().size());
        assertEquals(MetaColumnType.STRING,
                MetaTableDefinitionCodec.toColumnEntities(def).get(0).getDataType());
    }

    @Test
    void unknownPropertyFails() {
        String yaml = """
                tableCode: task
                tableName: 任务
                bogusField: x
                columns: []
                """;
        assertThrows(Exception.class, () -> MetaTableDefinitionCodec.fromYaml(yaml));
    }

    @Test
    void unknownDataTypeFails() {
        TableDefinition def = new TableDefinition();
        def.setTableCode("t");
        def.setTableName("t");
        ColumnDefinition col = new ColumnDefinition();
        col.setColumnCode("c");
        col.setColumnName("c");
        col.setDataType("NOPE");
        def.setColumns(List.of(col));
        assertThrows(IllegalArgumentException.class, () -> MetaTableDefinitionCodec.toColumnEntities(def));
    }

    /**
     * Guard against silent fidelity loss: every @Column-persistent field on
     * MetaColumn must have a same-named field on ColumnDefinition.
     */
    @Test
    void everyPersistentMetaColumnFieldIsMapped() {
        Set<String> mapped = new HashSet<>();
        for (Field f : ColumnDefinition.class.getDeclaredFields()) {
            mapped.add(f.getName());
        }
        // tableId is the foreign key back to the owning table — not definition data.
        Set<String> excluded = Set.of("tableId");
        for (Field f : MetaColumn.class.getDeclaredFields()) {
            if (f.isAnnotationPresent(Column.class) && !excluded.contains(f.getName())) {
                assertTrue(mapped.contains(f.getName()),
                        "persistent field " + f.getName() + " missing from ColumnDefinition");
            }
        }
    }

    @Test
    void everyPersistentMetaTableFieldIsMapped() {
        Set<String> mapped = new HashSet<>();
        for (Field f : TableDefinition.class.getDeclaredFields()) {
            mapped.add(f.getName());
        }
        Set<String> excluded = Set.of("version"); // optimistic-lock field, not definition data
        for (Field f : MetaTable.class.getDeclaredFields()) {
            if (f.isAnnotationPresent(Column.class) && !excluded.contains(f.getName())) {
                assertTrue(mapped.contains(f.getName()),
                        "persistent field " + f.getName() + " missing from TableDefinition");
            }
        }
    }

    @Test
    void columnMatchesTreatsAbsentFileKeysAsUnmanaged() {
        ColumnDefinition file = column("title", "标题");
        ColumnDefinition db = column("title", "标题");
        db.setSearchable(true);
        db.setLength(0);
        db.setIndexType("BTREE");
        assertTrue(MetaTableDefinitionCodec.matches(file, db));
    }

    @Test
    void columnMatchesDetectsPresentKeyMismatch() {
        ColumnDefinition file = column("title", "大标题");
        ColumnDefinition db = column("title", "标题");
        assertFalse(MetaTableDefinitionCodec.matches(file, db));

        file.setColumnName("标题");
        file.setSearchable(false);
        db.setSearchable(true);
        assertFalse(MetaTableDefinitionCodec.matches(file, db));
    }

    @Test
    void tableMatchesIgnoresColumnsAndAbsentKeys() {
        TableDefinition file = new TableDefinition();
        file.setTableCode("t");
        file.setTableName("T");
        file.setColumns(List.of(column("a", "A")));
        TableDefinition db = new TableDefinition();
        db.setTableCode("t");
        db.setTableName("T");
        db.setDescription("");
        db.setTablePrefix("meta_");
        db.setStatus(1);
        assertTrue(MetaTableDefinitionCodec.matches(file, db));

        file.setStatus(0);
        assertFalse(MetaTableDefinitionCodec.matches(file, db));
    }

    @Test
    void applyToKeepsIdentityAuditAndUnmanagedFields() {
        MetaColumn managed = new MetaColumn();
        managed.setId(9L);
        managed.setCreatorId(5L);
        managed.setColumnCode("title");
        managed.setColumnName("标题");
        managed.setDataType(MetaColumnType.STRING);
        managed.setSearchable(true);
        ColumnDefinition file = column("title", "大标题");
        file.setRequired(true);

        MetaTableDefinitionCodec.applyTo(file, managed);

        assertEquals("大标题", managed.getColumnName());
        assertEquals(true, managed.getRequired());
        assertEquals(Long.valueOf(9L), managed.getId());
        assertEquals(Long.valueOf(5L), managed.getCreatorId());
        assertEquals(true, managed.getSearchable());

        MetaTable table = new MetaTable();
        table.setId(3L);
        table.setVersion(4);
        table.setCreatorId(5L);
        table.setDescription("kept");
        TableDefinition def = new TableDefinition();
        def.setTableCode("t");
        def.setTableName("新名");

        MetaTableDefinitionCodec.applyTo(def, table);

        assertEquals("新名", table.getTableName());
        assertEquals("kept", table.getDescription());
        assertEquals(Integer.valueOf(4), table.getVersion());
        assertEquals(Long.valueOf(5L), table.getCreatorId());
    }

    private static ColumnDefinition column(String code, String name) {
        ColumnDefinition col = new ColumnDefinition();
        col.setColumnCode(code);
        col.setColumnName(name);
        col.setDataType("STRING");
        return col;
    }

    @Test
    void yamlKeyOrderIsStable() {
        MetaTable table = new MetaTable();
        table.setTableCode("a");
        table.setTableName("A");
        String first = MetaTableDefinitionCodec.toYaml(
                MetaTableDefinitionCodec.toDefinition(table, List.of()));
        String second = MetaTableDefinitionCodec.toYaml(
                MetaTableDefinitionCodec.toDefinition(table, List.of()));
        assertEquals(first, second);
        assertTrue(first.indexOf("tableCode") < first.indexOf("columns"),
                "tableCode should precede columns");
    }
}
