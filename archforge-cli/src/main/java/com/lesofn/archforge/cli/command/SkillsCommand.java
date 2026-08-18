package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.skill.SkillInstaller;
import java.util.Set;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "skills",
        description = "Install ArchForge skills into AI tools",
        subcommands = {
                SkillsCommand.Install.class,
                SkillsCommand.List.class,
                SkillsCommand.Update.class,
                SkillsCommand.Remove.class
        })
public class SkillsCommand {

    static final Set<String> TOOLS = Set.of("claude", "codex", "cursor", "devin");

    @Command(name = "install", description = "Append skills to the selected tool")
    static class Install implements Callable<Integer> {
        @Option(names = "--tool", required = true)
        String tool;

        @Override
        public Integer call() {
            if (!TOOLS.contains(tool)) {
                System.err.println("Unsupported tool: " + tool + ". Allowed: " + TOOLS);
                return 1;
            }
            SkillInstaller.install(ProjectPaths.repoRoot(), tool);
            System.out.println("Installed ArchForge skills for " + tool);
            return 0;
        }
    }

    @Command(name = "list", description = "List supported tools")
    static class List implements Callable<Integer> {
        @Override
        public Integer call() {
            TOOLS.forEach(System.out::println);
            return 0;
        }
    }

    @Command(name = "update", description = "Re-apply skill snippets")
    static class Update implements Callable<Integer> {
        @Option(names = "--tool", required = true)
        String tool;

        @Override
        public Integer call() {
            Install install = new Install();
            install.tool = this.tool;
            return install.call();
        }
    }

    @Command(name = "remove", description = "Remove generated skill block")
    static class Remove implements Callable<Integer> {
        @Option(names = "--tool", required = true)
        String tool;

        @Override
        public Integer call() {
            SkillInstaller.remove(ProjectPaths.repoRoot(), tool);
            return 0;
        }
    }
}
