package com.lesofn.archforge.meta.table.api.definition;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.meta.table.internal.validator.MetaTableValidator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

/**
 * Pure-file CI gate (P3-4-3 F1/F2): every {@code project-definition/meta/*.yaml}
 * committed in the repo must parse under the strict codec and pass the
 * designer's DB-less definition validation (identifiers, types, display
 * expressions) — a file that would fail the file-mode startup apply fails the
 * normal {@code test} task first. Skips silently outside the ArchForge repo
 * (generated projects have no {@code project-definition/} dir).
 */
class RepoDefinitionSanityTest {

    @Test
    void committedDefinitionFilesParseStrictlyAndValidate() {
        Path dir = findDefinitionDir();
        if (dir == null) {
            return; // not the ArchForge repo checkout — nothing to verify
        }
        Map<String, String> docs = new FsDefinitionSource(dir).load();
        assertFalse(docs.isEmpty(), dir + " exists but holds no *.yaml definitions");
        // No repositories wired: cross-table reference checks need a DB and are left to the startup apply.
        MetaTableValidator validator = new MetaTableValidator();
        docs.forEach((name, yaml) -> {
            TableDefinition def = MetaTableDefinitionCodec.fromYaml(yaml);
            assertEqualsFileName(name, def.getTableCode());
            validator.validate(MetaTableDefinitionCodec.toTableEntity(def), MetaTableDefinitionCodec.toColumnEntities(def));
        });
    }

    private static void assertEqualsFileName(String fileName, String tableCode) {
        String expected = tableCode + ".yaml";
        assertTrue(expected.equals(fileName),
                "definition file " + fileName + " must be named <tableCode>.yaml (" + expected + ")");
    }

    @Nullable
    private static Path findDefinitionDir() {
        Path cursor = Path.of("").toAbsolutePath();
        for (int i = 0; cursor != null && i < 6; i++, cursor = cursor.getParent()) {
            if (Files.exists(cursor.resolve("settings.gradle.kts"))) {
                Path dir = cursor.resolve("project-definition/meta");
                return Files.isDirectory(dir) ? dir : null;
            }
        }
        return null;
    }
}
