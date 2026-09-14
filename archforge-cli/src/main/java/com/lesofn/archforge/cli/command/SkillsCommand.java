package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.skill.SkillInstaller;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(
        mixinStandardHelpOptions = true,
        name = "skills",
        description = "Install ArchForge skill snippets into AI coding tools",
        subcommands = {
                SkillsCommand.Install.class,
                SkillsCommand.Tools.class,
                SkillsCommand.Remove.class,
                SkillsCommand.Update.class
        })
public class SkillsCommand implements Callable<Integer> {

    @Spec
    CommandSpec spec;

    static final Map<String, String> TOOLS = Map.of(
            "claude", "CLAUDE.md",
            "codex", "AGENTS.md",
            "devin", "AGENTS.md",
            "cursor", ".cursor/rules/archforge.mdc");

    @Override
    public Integer call() {
        spec.commandLine().usage(spec.commandLine().getOut());
        return 0;
    }

    @Command(
            mixinStandardHelpOptions = true,
            name = "install",
            description = "Append the ArchForge skill block to the tool's config file")
    static class Install implements Callable<Integer> {
        @Parameters(index = "0", description = "Target tool: ${COMPLETION-CANDIDATES}",
                completionCandidates = ToolCandidates.class)
        String tool;

        @Override
        public Integer call() {
            if (!TOOLS.containsKey(tool)) {
                System.err.println("Unsupported tool: " + tool + ". Allowed: " + TOOLS.keySet());
                return 1;
            }
            SkillInstaller.install(ProjectPaths.repoRoot(), tool);
            System.out.println("Installed ArchForge skills for " + tool + " → " + TOOLS.get(tool));
            return 0;
        }
    }

    @Command(
            mixinStandardHelpOptions = true,
            name = "tools",
            aliases = {
                    "list"
            },
            description = "Show supported tools and their target files (alias: list)")
    static class Tools implements Callable<Integer> {
        @Override
        public Integer call() {
            TOOLS.forEach((tool, target) -> System.out.println("  " + tool + " -> " + target));
            return 0;
        }
    }

    @Command(
            mixinStandardHelpOptions = true,
            name = "update",
            hidden = true,
            description = "Alias for `skills install`")
    static class Update implements Callable<Integer> {
        @Parameters(index = "0", completionCandidates = ToolCandidates.class)
        String tool;

        @Override
        public Integer call() {
            Install install = new Install();
            install.tool = this.tool;
            return install.call();
        }
    }

    @Command(
            mixinStandardHelpOptions = true,
            name = "remove",
            description = "Remove the generated skill block from the tool's config file")
    static class Remove implements Callable<Integer> {
        @Parameters(index = "0", description = "Target tool: ${COMPLETION-CANDIDATES}",
                completionCandidates = ToolCandidates.class)
        String tool;

        @Override
        public Integer call() {
            SkillInstaller.remove(ProjectPaths.repoRoot(), tool);
            System.out.println("Removed ArchForge skills block for " + tool);
            return 0;
        }
    }

    static class ToolCandidates extends java.util.ArrayList<String> {
        ToolCandidates() {
            super(TOOLS.keySet());
        }
    }
}
