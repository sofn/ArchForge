package com.lesofn.archforge.meta.table.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The REFERENCE display expression is rendered into the list/export SELECT, so it is a whitelist
 * grammar (ref columns, string literals, {@code ||}) — never a blacklist over arbitrary SQL.
 */
class DisplayExpressionTest {

    @Test
    void rendersRefColumnsLiteralsAndConcatenation() {
        assertEquals("ref_owner.\"username\"", DisplayExpression.render("ref.username", "ref_owner"));
        assertEquals("ref_owner.\"username\" || ' - ' || ref_owner.\"email\"",
                DisplayExpression.render("ref.username || ' - ' || ref.email", "ref_owner"));
        assertEquals("ref_owner.\"id\"::text || 'x'", DisplayExpression.render(" REF.Id::TEXT||'x' ", "ref_owner"));
        assertEquals("'it''s' || ref_owner.\"code\"", DisplayExpression.render("'it''s' || ref.code", "ref_owner"));
    }

    @Test
    void literalContentIsInertEvenWhenItLooksLikeSql() {
        String literal = "'; DROP TABLE sys_user; -- ref.x'";

        assertEquals(literal + " || ref_a.\"b\"", DisplayExpression.render(literal + " || ref.b", "ref_a"));
    }

    @Test
    void rejectsEverythingOutsideTheGrammar() {
        List<String> rejected = List.of(
                "pg_sleep(5)",
                "current_setting('server_version')",
                "version()",
                "CASE WHEN 1=1 THEN ref.id ELSE 0 END",
                "(ref.id)",
                "ref.id::int",
                "ref.id + 1",
                "COALESCE(ref.name, '')",
                "ref.name || pg_sleep(1)::text",
                "ref.id;",
                "ref.id -- x",
                "ref.id /* x */",
                "ref.a ref.b",
                "'a' 'b'",
                "'unterminated",
                "ref.",
                "ref.1a",
                "other.name",
                "ref.name ||",
                "|| ref.name",
                "ref.name || || ref.name",
                // The old blacklist matched with ".*KEYWORD.*", which never crosses a line break.
                "ref.id ||\n(SELECT password FROM sys_user LIMIT 1)",
                "ref.id\r\n|| (select 1)",
                "ref.\"id\"",
                "");
        for (String expression : rejected) {
            assertThrows(MetaTableException.class, () -> DisplayExpression.render(expression, "ref_x"), expression);
        }
    }

    @Test
    void rejectsOversizedExpressions() {
        String huge = "ref.a" + " || ref.a".repeat(200);

        assertThrows(MetaTableException.class, () -> DisplayExpression.render(huge, "ref_x"));
    }
}
