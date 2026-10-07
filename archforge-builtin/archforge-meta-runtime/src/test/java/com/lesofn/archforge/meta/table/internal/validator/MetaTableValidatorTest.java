package com.lesofn.archforge.meta.table.internal.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.meta.table.api.dao.MetaColumnRepository;
import com.lesofn.archforge.meta.table.api.dao.MetaTableRepository;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Covers update-path required checks and the REFERENCE fan-out guard. */
class MetaTableValidatorTest {

    private MetaTableValidator validator;
    private MetaTableRepository metaTableRepository;
    private MetaColumnRepository metaColumnRepository;

    @BeforeEach
    void setUp() {
        validator = new MetaTableValidator();
        metaTableRepository = mock(MetaTableRepository.class);
        metaColumnRepository = mock(MetaColumnRepository.class);
        validator.setMetaTableRepository(metaTableRepository);
        validator.setMetaColumnRepository(metaColumnRepository);
    }

    @Test
    void decimalWithPrecisionButNoScaleValidates() {
        // Definition files may omit scale (unmanaged key → DB default 0); validation must not unbox it.
        MetaColumn amount = new MetaColumn();
        amount.setColumnCode("amount");
        amount.setColumnName("金额");
        amount.setDataType(MetaColumnType.DECIMAL);
        amount.setPrecision(10);

        assertDoesNotThrow(() -> validator.validate(table(), List.of(amount)));
    }

    @Test
    void unquotedDefaultValuesAreRejectedBeforeStorage() {
        for (MetaColumnType type : List.of(MetaColumnType.INTEGER, MetaColumnType.DECIMAL, MetaColumnType.FILE,
                MetaColumnType.IMAGE)) {
            MetaColumn column = column("amount", type);
            column.setDefaultValue("0, CONSTRAINT chk_probe CHECK (amount >= 0)");

            MetaTableException e = assertThrows(MetaTableException.class,
                    () -> validator.validate(table(), List.of(column)), type.name());
            assertEquals(MetaTableErrorCode.META_COLUMN_VALUE_INVALID.getCode(), e.getErrorInfo().getCode(), type.name());
        }
    }

    @Test
    void arrayElementDefaultsAreTypeChecked() {
        for (String elementType : List.of("INTEGER", "DECIMAL", "BOOLEAN")) {
            MetaColumn column = column("tags", MetaColumnType.ARRAY);
            column.setArrayElementType(elementType);
            column.setDefaultValue("[1]::bigint[]); DROP TABLE sys_user; --]");

            assertThrows(MetaTableException.class, () -> validator.validate(table(), List.of(column)), elementType);
        }
    }

    @Test
    void wellFormedDefaultValuesPass() {
        MetaColumn integer = column("qty", MetaColumnType.INTEGER);
        integer.setDefaultValue("7");
        MetaColumn decimal = column("price", MetaColumnType.DECIMAL);
        decimal.setDefaultValue("0.50");
        MetaColumn text = column("note", MetaColumnType.STRING);
        text.setDefaultValue("it's fine; -- really");
        MetaColumn ints = column("ids", MetaColumnType.ARRAY);
        ints.setArrayElementType("INTEGER");
        ints.setDefaultValue("[1, 2, 3]");
        MetaColumn day = column("since", MetaColumnType.DATE);
        day.setDefaultValue("2020-01-01");

        assertDoesNotThrow(() -> validator.validate(table(), List.of(integer, decimal, text, ints, day)));
    }

    @Test
    void displayExpressionFunctionCallsAreRejected() {
        for (String expression : List.of("pg_sleep(5)", "current_setting('server_version')", "version()",
                "CASE WHEN 1=1 THEN ref.id ELSE 0 END")) {
            assertDisplayExpressionRejected(expression);
        }
    }

    /** The old blacklist used String.matches(".*KEYWORD.*"), which cannot cross a line break. */
    @Test
    void displayExpressionLineBreakCannotHideASubquery() {
        assertDisplayExpressionRejected("ref.id ||\n(SELECT password FROM sys_user LIMIT 1)");
        assertDisplayExpressionRejected("ref.id\r\n|| (select 1)");
    }

    @Test
    void displayExpressionWithinTheGrammarPasses() {
        when(metaTableRepository.findAllByDeletedFalse()).thenReturn(List.of(targetTable()));
        MetaColumn ok = referenceColumn("meta_orders", "id");
        ok.setDisplayExpression("ref.id || ' / ' || ref.id::text");

        assertDoesNotThrow(() -> validator.validate(table(), List.of(ok)));
    }

    /** Pointing at {@code id} used to skip every check: {@code sys_user} + {@code ref.password} read password hashes. */
    @Test
    void referenceMustTargetThisTableOrARegisteredMetaTable() {
        when(metaTableRepository.findAllByDeletedFalse()).thenReturn(List.of(targetTable()));

        for (String target : List.of("sys_user", "sys_config", "cms_article")) {
            MetaColumn leak = referenceColumn(target, "id");
            leak.setDisplayExpression("ref.id");

            MetaTableException e = assertThrows(MetaTableException.class,
                    () -> validator.validate(table(), List.of(leak)), target);
            assertEquals(MetaTableErrorCode.META_COLUMN_TYPE_INVALID.getCode(), e.getErrorInfo().getCode(), target);
        }
    }

    private void assertDisplayExpressionRejected(String expression) {
        MetaColumn column = referenceColumn("order_ref", "id");
        column.setDisplayExpression(expression);

        MetaTableException e = assertThrows(MetaTableException.class,
                () -> validator.validate(table(), List.of(column)), expression);
        assertEquals(MetaTableErrorCode.META_COLUMN_VALUE_INVALID.getCode(), e.getErrorInfo().getCode(), expression);
    }

    @Test
    void tablePrefixCannotTurnACodeIntoAPlatformTable() {
        for (String prefix : List.of("sys_", "pg_", "qrtz_", "flyway_", "Meta_", "meta-")) {
            MetaTable table = table();
            table.setTableCode("menu");
            table.setTablePrefix(prefix);

            assertThrows(MetaTableException.class,
                    () -> validator.validate(table, List.of(column("name", MetaColumnType.STRING))), prefix);
        }
    }

    @Test
    void emptyPrefixKeepsWorkingForAdoptedPhysicalTables() {
        MetaTable adopted = table();
        adopted.setTableCode("legacy_goods");
        adopted.setTablePrefix("");

        assertDoesNotThrow(() -> validator.validate(adopted, List.of(column("name", MetaColumnType.STRING))));
    }

    @Test
    void insertRejectsMissingRequiredValue() {
        MetaColumn column = requiredColumn("name");
        Map<String, Object> row = new HashMap<>();

        assertThrows(MetaTableException.class, () -> validator.validateValues(row, List.of(column), true));
    }

    @Test
    void updateAbsentRequiredKeyRemainsAllowed() {
        MetaColumn column = requiredColumn("name");
        Map<String, Object> row = new HashMap<>();

        assertDoesNotThrow(() -> validator.validateValues(row, List.of(column), false));
    }

    @Test
    void updateExplicitNullForRequiredRejected() {
        MetaColumn column = requiredColumn("name");
        Map<String, Object> row = new HashMap<>();
        row.put("name", null);

        assertThrows(MetaTableException.class, () -> validator.validateValues(row, List.of(column), false));
    }

    @Test
    void updateEmptyStringForRequiredRejected() {
        MetaColumn column = requiredColumn("name");
        Map<String, Object> row = new HashMap<>();
        row.put("name", "");

        assertThrows(MetaTableException.class, () -> validator.validateValues(row, List.of(column), false));
    }

    @Test
    void referenceToNonUniqueTargetRejected() {
        stubTargetTable(column("buyer_id", false));

        MetaColumn reference = referenceColumn("meta_orders", "buyer_id");

        assertThrows(MetaTableException.class,
                () -> validator.validate(table(), List.of(reference)));
    }

    @Test
    void referenceToUniqueTargetAccepted() {
        stubTargetTable(column("order_no", true));

        MetaColumn reference = referenceColumn("meta_orders", "order_no");

        assertDoesNotThrow(() -> validator.validate(table(), List.of(reference)));
    }

    @Test
    void referenceToMissingColumnRejected() {
        stubTargetTable(column("buyer_id", false));

        MetaColumn reference = referenceColumn("meta_orders", "ghost");

        assertThrows(MetaTableException.class,
                () -> validator.validate(table(), List.of(reference)));
    }

    @Test
    void referenceToPrimaryKeyNeedsNoMetadataRow() {
        when(metaTableRepository.findAllByDeletedFalse()).thenReturn(List.of(targetTable()));

        MetaColumn reference = referenceColumn("meta_orders", "id");

        assertDoesNotThrow(() -> validator.validate(table(), List.of(reference)));
    }

    private void stubTargetTable(MetaColumn targetColumn) {
        MetaTable target = targetTable();
        when(metaTableRepository.findAllByDeletedFalse()).thenReturn(List.of(target));
        when(metaColumnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(java.util.Objects.requireNonNull(target.getId())))
                .thenReturn(List.of(targetColumn));
    }

    private MetaTable table() {
        MetaTable table = new MetaTable();
        table.setTableCode("reviews");
        table.setTablePrefix("meta_");
        return table;
    }

    private MetaTable targetTable() {
        MetaTable target = new MetaTable();
        target.setId(99L);
        target.setTableCode("orders");
        target.setTablePrefix("meta_");
        return target;
    }

    private MetaColumn referenceColumn(String refTable, String refColumn) {
        MetaColumn column = new MetaColumn();
        column.setColumnCode("order_ref");
        column.setColumnName("order_ref");
        column.setDataType(MetaColumnType.REFERENCE);
        column.setReferenceTable(refTable);
        column.setReferenceColumn(refColumn);
        column.setDisplayExpression("ref." + refColumn);
        return column;
    }

    private MetaColumn requiredColumn(String code) {
        MetaColumn column = new MetaColumn();
        column.setColumnCode(code);
        column.setColumnName(code);
        column.setDataType(MetaColumnType.STRING);
        column.setRequired(true);
        return column;
    }

    private MetaColumn column(String code, MetaColumnType type) {
        MetaColumn column = new MetaColumn();
        column.setColumnCode(code);
        column.setColumnName(code);
        column.setDataType(type);
        return column;
    }

    private MetaColumn column(String code, boolean unique) {
        MetaColumn column = new MetaColumn();
        column.setColumnCode(code);
        column.setColumnName(code);
        column.setDataType(MetaColumnType.STRING);
        column.setUnique(unique);
        return column;
    }
}
