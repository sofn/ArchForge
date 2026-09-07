package com.lesofn.archforge.cli.skill;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SkillInstaller {

    static final String BEGIN = "<!-- ARCHFORGE-SKILLS:BEGIN -->";
    static final String END = "<!-- ARCHFORGE-SKILLS:END -->";
    static final String BLOCK = BEGIN + "\n" + "ArchForge CLI skills installed.\n" +
            "- Use `./archforge --help` for command tree.\n" + "- Backend modules are prefixed with `archforge-`.\n" +
            "- Contracts live in spec/openapi.yaml.\n" + END + "\n";

    private SkillInstaller() {
    }

    public static void install(Path repoRoot, String tool) {
        Path target = targetFile(repoRoot, tool);
        try {
            Files.createDirectories(target.getParent() == null ? repoRoot : target.getParent());
            String existing = Files.exists(target) ? Files.readString(target, StandardCharsets.UTF_8) : "";
            String without = strip(existing).stripTrailing();
            String updated = (without.isBlank() ? "" : without + "\n\n") + BLOCK;
            Files.writeString(target, updated, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to install skills for " + tool, e);
        }
    }

    public static void remove(Path repoRoot, String tool) {
        Path target = targetFile(repoRoot, tool);
        if (!Files.exists(target)) {
            return;
        }
        try {
            Files.writeString(target, strip(Files.readString(target, StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to remove skills for " + tool, e);
        }
    }

    static Path targetFile(Path repoRoot, String tool) {
        return switch (tool) {
            case "claude" -> repoRoot.resolve("CLAUDE.md");
            case "codex", "devin" -> repoRoot.resolve("AGENTS.md");
            case "cursor" -> repoRoot.resolve(".cursor").resolve("rules").resolve("archforge.mdc");
            default -> throw new IllegalArgumentException("Unsupported tool: " + tool);
        };
    }

    private static String strip(String content) {
        int start = content.indexOf(BEGIN);
        int end = content.indexOf(END);
        if (start >= 0 && end > start) {
            return content.substring(0, start) + content.substring(end + END.length());
        }
        return content;
    }
}
