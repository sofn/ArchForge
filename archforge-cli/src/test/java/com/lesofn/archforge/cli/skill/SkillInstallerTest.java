package com.lesofn.archforge.cli.skill;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SkillInstallerTest {

    @TempDir
    Path tempDir;

    @Test
    void installAppendsClaudeBlockOnce() throws Exception {
        Files.writeString(tempDir.resolve("CLAUDE.md"), "# existing\n");
        SkillInstaller.install(tempDir, "claude");
        SkillInstaller.install(tempDir, "claude");
        String content = Files.readString(tempDir.resolve("CLAUDE.md"));
        assertTrue(content.contains("# existing"));
        int first = content.indexOf(SkillInstaller.BEGIN);
        int last = content.lastIndexOf(SkillInstaller.BEGIN);
        assertTrue(first >= 0);
        assertTrue(first == last);
    }
}
