package com.lesofn.archforge.meta.table.internal.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.meta.table.api.dao.MetaColumnRepository;
import com.lesofn.archforge.meta.table.api.dao.MetaTableRepository;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
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
        when(metaColumnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(target.getId()))
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

    private MetaColumn column(String code, boolean unique) {
        MetaColumn column = new MetaColumn();
        column.setColumnCode(code);
        column.setColumnName(code);
        column.setDataType(MetaColumnType.STRING);
        column.setUnique(unique);
        return column;
    }
}
