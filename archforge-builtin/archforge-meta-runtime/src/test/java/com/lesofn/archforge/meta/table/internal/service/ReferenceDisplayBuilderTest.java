package com.lesofn.archforge.meta.table.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

/** The display expression is stored data — the builder must never emit anything outside the grammar. */
class ReferenceDisplayBuilderTest {

    @Test
    void rendersRefColumnsAgainstTheJoinAlias() {
        MetaColumn column = reference("owner_id", "ref.username || ' - ' || ref.email");

        assertEquals("ref_owner_id.\"username\" || ' - ' || ref_owner_id.\"email\"",
                ReferenceDisplayBuilder.buildDisplayExpression(column, "main"));
    }

    @Test
    void blankExpressionFallsBackToTheRawReferenceValue() {
        assertEquals("main.\"owner_id\"", ReferenceDisplayBuilder.buildDisplayExpression(reference("owner_id", " "), "main"));
        assertEquals("main.\"owner_id\"", ReferenceDisplayBuilder.buildDisplayExpression(reference("owner_id", null), "main"));
    }

    @Test
    void storedExpressionOutsideTheGrammarNeverReachesTheSelectList() {
        for (String stored : List.of("pg_sleep(5)", "ref.id ||\n(SELECT password FROM sys_user LIMIT 1)",
                "COALESCE(ref.name, '')")) {
            String rendered = ReferenceDisplayBuilder.buildDisplayExpression(reference("owner_id", stored), "main");

            // degrade to the raw reference value: the list keeps working, no stored SQL is executed
            assertEquals("main.\"owner_id\"", rendered, stored);
        }
    }

    @Test
    void selectColumnsCarryTheRenderedDisplayColumn() {
        MetaColumn column = reference("owner_id", "ref.username");

        List<String> columns = ReferenceDisplayBuilder.buildSelectColumns(List.of(column), "main");

        assertTrue(columns.contains("ref_owner_id.\"username\" AS \"owner_id_display\""), columns.toString());
        assertFalse(columns.stream().anyMatch(c -> c.contains("SELECT")), columns.toString());
    }

    /** Legacy rows may still point at a platform table: never join it (sys_user.password must not leak). */
    @Test
    void referenceToAPlatformTableIsNeitherJoinedNorRendered() {
        MetaColumn column = reference("owner_id", "ref.password");
        column.setReferenceTable("sys_user");

        assertEquals(List.of(), ReferenceDisplayBuilder.buildJoins(List.of(column), "main"));
        assertEquals("main.\"owner_id\"", ReferenceDisplayBuilder.buildDisplayExpression(column, "main"));
    }

    private MetaColumn reference(String code, @Nullable String expression) {
        MetaColumn column = new MetaColumn();
        column.setColumnCode(code);
        column.setColumnName(code);
        column.setDataType(MetaColumnType.REFERENCE);
        column.setReferenceTable("meta_owner");
        column.setReferenceColumn("id");
        if (expression != null) {
            column.setDisplayExpression(expression);
        }
        return column;
    }
}
