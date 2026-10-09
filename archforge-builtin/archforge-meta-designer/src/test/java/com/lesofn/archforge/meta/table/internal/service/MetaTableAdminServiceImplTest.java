package com.lesofn.archforge.meta.table.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.meta.table.internal.dao.MetaColumnRepository;
import org.jspecify.annotations.Nullable;
import com.lesofn.archforge.meta.table.internal.dao.MetaTableRepository;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import com.lesofn.archforge.meta.table.api.service.MetaTableMigrationService;
import com.lesofn.archforge.meta.table.internal.ddl.AlterTableDdlGenerator;
import com.lesofn.archforge.meta.table.internal.ddl.MetaTableDdlGenerator;
import com.lesofn.archforge.meta.table.internal.ddl.SchemaDdl;
import com.lesofn.archforge.meta.table.internal.schema.SchemaChange;
import com.lesofn.archforge.meta.table.internal.schema.SchemaChangeType;
import com.lesofn.archforge.meta.table.internal.schema.SchemaDiffEngine;
import com.lesofn.archforge.meta.table.internal.validator.MetaTableValidator;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InOrder;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

/** Unit coverage for optimistic-lock error mapping and evolution pre-flight orchestration. */
class MetaTableAdminServiceImplTest {

    private static final long TABLE_ID = 7L;

    private MetaTableRepository metaTableRepository;
    private MetaColumnRepository metaColumnRepository;
    private JdbcOperations jdbcOperations;
    private SchemaDiffEngine schemaDiffEngine;
    private AlterTableDdlGenerator alterTableDdlGenerator;
    private MetaTableMigrationService migrationService;
    private MetaTableAdminServiceImpl service;
    private MetaTable existing;

    @BeforeEach
    void setUp() {
        metaTableRepository = mock(MetaTableRepository.class);
        metaColumnRepository = mock(MetaColumnRepository.class);
        jdbcOperations = mock(JdbcOperations.class);
        schemaDiffEngine = mock(SchemaDiffEngine.class);
        alterTableDdlGenerator = mock(AlterTableDdlGenerator.class);
        migrationService = mock(MetaTableMigrationService.class);

        existing = new MetaTable();
        existing.setId(TABLE_ID);
        existing.setTableCode("orders");
        existing.setTableName("订单");
        existing.setTablePrefix("meta_");
        when(metaTableRepository.findById(TABLE_ID)).thenReturn(Optional.of(existing));
        when(metaColumnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(TABLE_ID))
                .thenReturn(List.of(existingColumn()));

        service = serviceWith(new MockEnvironment());
    }

    private MetaTableAdminServiceImpl serviceWith(MockEnvironment environment) {
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        when(jdbcTemplate.getJdbcOperations()).thenReturn(jdbcOperations);
        return new MetaTableAdminServiceImpl(metaTableRepository, metaColumnRepository, mock(
                MetaTableDdlGenerator.class), mock(
                        MetaTableValidator.class), jdbcTemplate, schemaDiffEngine, alterTableDdlGenerator, migrationService, new DefinitionWriteGuard(environment));
    }

    @Test
    void fileSourceRejectsEveryDefinitionWrite() {
        MetaTableAdminServiceImpl guarded = serviceWith(
                new MockEnvironment().withProperty("arch-forge.meta.source", "file"));
        List<Executable> writes = List.of(
                () -> guarded.create(incoming(), List.of(newColumn())),
                () -> guarded.update(TABLE_ID, incoming(), List.of(newColumn()), 1L),
                () -> guarded.updateMeta(TABLE_ID, incoming(), 1L),
                () -> guarded.copy(TABLE_ID),
                () -> guarded.delete(TABLE_ID, true));

        for (Executable write : writes) {
            MetaTableException e = assertThrows(MetaTableException.class, write);
            assertEquals(MetaTableErrorCode.META_DEFINITION_FILE_MANAGED.getCode(), e.getErrorInfo().getCode());
        }
        verify(metaTableRepository, never()).save(any());
        verify(metaTableRepository, never()).saveAndFlush(any());
        verify(jdbcOperations, never()).execute(anyString());
    }

    @Test
    void concurrentModifyOnUpdateMapsToBusinessError() {
        stubDiff(List.of(), null, List.of());
        when(metaTableRepository.saveAndFlush(existing))
                .thenThrow(new ObjectOptimisticLockingFailureException(MetaTable.class, TABLE_ID));

        MetaTableException exception = assertThrows(
                MetaTableException.class,
                () -> service.update(TABLE_ID, incoming(), List.of(newColumn()), 1L));

        assertEquals("表定义已被他人修改，请刷新后重试", exception.getErrorInfo().getMsg());
    }

    @Test
    void concurrentModifyOnDeleteMapsToBusinessError() {
        when(metaTableRepository.saveAndFlush(existing))
                .thenThrow(new ObjectOptimisticLockingFailureException(MetaTable.class, TABLE_ID));

        MetaTableException exception = assertThrows(MetaTableException.class, () -> service.delete(TABLE_ID, true));

        assertEquals("表定义已被他人修改，请刷新后重试", exception.getErrorInfo().getMsg());
    }

    @Test
    void setNotNullWithNullsAndNoDefaultRejectedBeforeDdl() {
        SchemaChange change = nullabilityChange(true);
        stubDiff(List.of(change), "SELECT COUNT(*) NULLS", List.of());
        when(jdbcOperations.queryForObject("SELECT COUNT(*) NULLS", Long.class)).thenReturn(3L);
        when(alterTableDdlGenerator.buildBackfillUpdateSql(existing, change)).thenReturn(Optional.empty());

        assertThrows(MetaTableException.class,
                () -> service.update(TABLE_ID, incoming(), List.of(newColumn()), 1L));

        verify(jdbcOperations, never()).execute(anyString());
    }

    @Test
    void setNotNullWithDefaultBackfillsBeforeConstraint() {
        SchemaChange change = nullabilityChange(true);
        String alterSql = "ALTER TABLE \"meta_orders\" ALTER COLUMN \"name\" SET NOT NULL";
        stubDiff(List.of(change), "SELECT COUNT(*) NULLS", List.of(alterSql));
        when(jdbcOperations.queryForObject("SELECT COUNT(*) NULLS", Long.class)).thenReturn(2L);
        when(alterTableDdlGenerator.buildBackfillUpdateSql(existing, change))
                .thenReturn(Optional.of("UPDATE \"meta_orders\" SET \"name\" = 'n/a' WHERE \"name\" IS NULL"));

        service.update(TABLE_ID, incoming(), List.of(newColumn()), 1L);

        InOrder order = inOrder(jdbcOperations);
        order.verify(jdbcOperations).execute("UPDATE \"meta_orders\" SET \"name\" = 'n/a' WHERE \"name\" IS NULL");
        order.verify(jdbcOperations).execute(alterSql);
    }

    @Test
    void castFailureDuringTypePreflightMappedToBusinessError() {
        SchemaChange change = typeChange();
        stubDiff(List.of(change), "SELECT COUNT(*) LOSS", List.of());
        when(jdbcOperations.queryForObject("SELECT COUNT(*) LOSS", Long.class))
                .thenThrow(new BadSqlGrammarException("q", "sql", new SQLException("invalid input syntax for integer")));

        assertThrows(MetaTableException.class,
                () -> service.update(TABLE_ID, incoming(), List.of(newColumn()), 1L));

        verify(jdbcOperations, never()).execute(anyString());
    }

    @Test
    void lossCountAboveZeroRejectsTypeChange() {
        SchemaChange change = typeChange();
        stubDiff(List.of(change), "SELECT COUNT(*) LOSS", List.of());
        when(jdbcOperations.queryForObject("SELECT COUNT(*) LOSS", Long.class)).thenReturn(4L);

        assertThrows(MetaTableException.class,
                () -> service.update(TABLE_ID, incoming(), List.of(newColumn()), 1L));

        verify(jdbcOperations, never()).execute(anyString());
    }

    @Test
    void losslessTypeChangeExecutesDdl() {
        SchemaChange change = typeChange();
        String alterSql = "ALTER TABLE \"meta_orders\" ALTER COLUMN \"name\" TYPE BIGINT";
        stubDiff(List.of(change), "SELECT COUNT(*) LOSS", List.of(alterSql));
        when(jdbcOperations.queryForObject("SELECT COUNT(*) LOSS", Long.class)).thenReturn(0L);

        service.update(TABLE_ID, incoming(), List.of(newColumn()), 1L);

        verify(jdbcOperations).execute(alterSql);
    }

    private void stubDiff(List<SchemaChange> changes, @Nullable String violationSql, List<String> ddlSqls) {
        when(schemaDiffEngine.diff(any(), anyList(), anyList())).thenReturn(changes);
        when(alterTableDdlGenerator.generate(any(), anyList()))
                .thenReturn(changes.stream().map(change -> new SchemaDdl(change, ddlSqls)).toList());
        for (SchemaChange change : changes) {
            when(alterTableDdlGenerator.buildViolationCountSql(any(), eq(change)))
                    .thenReturn(violationSql == null ? Optional.empty() : Optional.of(violationSql));
        }
    }

    private MetaTable incoming() {
        MetaTable incoming = new MetaTable();
        incoming.setTableName("订单");
        return incoming;
    }

    private MetaColumn existingColumn() {
        MetaColumn column = new MetaColumn();
        column.setId(11L);
        column.setColumnCode("name");
        column.setColumnName("名称");
        column.setDataType(MetaColumnType.STRING);
        column.setLength(50);
        return column;
    }

    private MetaColumn newColumn() {
        MetaColumn column = existingColumn();
        column.setId(null);
        return column;
    }

    private SchemaChange nullabilityChange(boolean required) {
        MetaColumn oldColumn = existingColumn();
        MetaColumn newColumn = existingColumn();
        newColumn.setRequired(required);
        SchemaChange change = new SchemaChange();
        change.setType(SchemaChangeType.ALTER_NULL);
        change.setOldColumn(oldColumn);
        change.setNewColumn(newColumn);
        change.setNewRequired(required);
        return change;
    }

    private SchemaChange typeChange() {
        MetaColumn oldColumn = existingColumn();
        oldColumn.setDataType(MetaColumnType.STRING);
        MetaColumn newColumn = existingColumn();
        newColumn.setDataType(MetaColumnType.INTEGER);
        SchemaChange change = new SchemaChange();
        change.setType(SchemaChangeType.ALTER_TYPE);
        change.setOldColumn(oldColumn);
        change.setNewColumn(newColumn);
        change.setOldType("VARCHAR(50)");
        change.setNewType("BIGINT");
        return change;
    }
}
