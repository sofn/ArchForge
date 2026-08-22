package com.lesofn.archforge.meta.table.testing;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test data builder for {@link MetaTable} and its {@link MetaColumn}s. Produces a valid,
 * enabled table; columns are optional and configured through {@link #withColumn(String)}.
 */
public final class MetaTableTestBuilder {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private final MetaTable table = new MetaTable();
    private final List<MetaColumn> columns = new ArrayList<>();

    private MetaTableTestBuilder() {
    }

    public static MetaTableTestBuilder aTable() {
        return new MetaTableTestBuilder();
    }

    public MetaTableTestBuilder withId(Long id) {
        table.setId(id);
        return this;
    }

    public MetaTableTestBuilder withTableCode(String tableCode) {
        table.setTableCode(tableCode);
        return this;
    }

    public MetaTableTestBuilder withTableName(String tableName) {
        table.setTableName(tableName);
        return this;
    }

    public MetaTableTestBuilder withDescription(String description) {
        table.setDescription(description);
        return this;
    }

    public MetaTableTestBuilder withTablePrefix(String tablePrefix) {
        table.setTablePrefix(tablePrefix);
        return this;
    }

    public MetaTableTestBuilder withStatus(Integer status) {
        table.setStatus(status);
        return this;
    }

    public MetaTableTestBuilder disabled() {
        table.setStatus(0);
        return this;
    }

    public ColumnSpec withColumn(String columnCode) {
        return new ColumnSpec(columnCode);
    }

    public MetaTable build() {
        long seq = SEQUENCE.incrementAndGet();
        if (table.getTableCode() == null) {
            table.setTableCode("t" + seq);
        }
        if (table.getTableName() == null) {
            table.setTableName("元表" + seq);
        }
        if (table.getStatus() == null) {
            table.setStatus(1);
        }
        if (table.getSchemaVersion() == null) {
            table.setSchemaVersion(1);
        }
        if (table.getDeleted() == null) {
            table.setDeleted(false);
        }
        return table;
    }

    /** Built columns get {@code tableId} from the built table. Call after {@link #build()}. */
    public List<MetaColumn> buildColumns() {
        columns.forEach(column -> column.setTableId(table.getId()));
        return List.copyOf(columns);
    }

    public final class ColumnSpec {

        private final MetaColumn column;

        private ColumnSpec(String columnCode) {
            this.column = new MetaColumn();
            this.column.setColumnCode(columnCode);
        }

        public ColumnSpec named(String columnName) {
            column.setColumnName(columnName);
            return this;
        }

        public ColumnSpec ofType(MetaColumnType dataType) {
            column.setDataType(dataType);
            return this;
        }

        public ColumnSpec withLength(int length) {
            column.setLength(length);
            return this;
        }

        public ColumnSpec required() {
            column.setRequired(true);
            column.setNullable(false);
            return this;
        }

        public ColumnSpec unique() {
            column.setUnique(true);
            return this;
        }

        public ColumnSpec searchable() {
            column.setSearchable(true);
            return this;
        }

        public ColumnSpec listVisible() {
            column.setListVisible(true);
            return this;
        }

        public MetaTableTestBuilder add() {
            long seq = SEQUENCE.incrementAndGet();
            if (column.getColumnName() == null) {
                column.setColumnName(column.getColumnCode());
            }
            if (column.getDataType() == null) {
                column.setDataType(MetaColumnType.STRING);
            }
            if (column.getNullable() == null) {
                column.setNullable(!Boolean.TRUE.equals(column.getRequired()));
            }
            if (column.getSort() == null) {
                column.setSort(columns.size() + 1);
            }
            if (column.getListVisible() == null) {
                column.setListVisible(false);
            }
            if (column.getSearchable() == null) {
                column.setSearchable(false);
            }
            if (column.getDeleted() == null) {
                column.setDeleted(false);
            }
            columns.add(column);
            return MetaTableTestBuilder.this;
        }
    }
}
