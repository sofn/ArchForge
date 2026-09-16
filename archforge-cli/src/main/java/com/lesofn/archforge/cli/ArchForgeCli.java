package com.lesofn.archforge.cli;

import com.lesofn.archforge.cli.command.BuildCommand;
import com.lesofn.archforge.cli.command.DbCommand;
import com.lesofn.archforge.cli.command.DevCommand;
import com.lesofn.archforge.cli.command.DoctorCommand;
import com.lesofn.archforge.cli.command.DownCommand;
import com.lesofn.archforge.cli.command.InfraCommand;
import com.lesofn.archforge.cli.command.InitCommand;
import com.lesofn.archforge.cli.command.LogsCommand;
import com.lesofn.archforge.cli.command.McpCommand;
import com.lesofn.archforge.cli.command.MetaCommand;
import com.lesofn.archforge.cli.command.ModuleCommand;
import com.lesofn.archforge.cli.command.NewCommand;
import com.lesofn.archforge.cli.command.RestartCommand;
import com.lesofn.archforge.cli.command.SkillsCommand;
import com.lesofn.archforge.cli.command.StatusCommand;
import com.lesofn.archforge.cli.command.UpCommand;
import com.lesofn.archforge.cli.mcp.McpServerMode;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;

@Command(
        name = "archforge",
        mixinStandardHelpOptions = true,
        versionProvider = ArchForgeCli.ManifestVersionProvider.class,
        description = "ArchForge developer CLI — dev stack, containers, database, AI tooling.",
        footer = {
                "",
                "Examples:",
                "  archforge init --write        one-time setup: .env secrets + deps + migrate",
                "  archforge dev                 run backend bootRun + pnpm dev (detached, logs/)",
                "  archforge up                  run the whole stack as containers",
                "  archforge status              what's running right now",
                "  archforge down -v             stop everything AND drop dev data",
                "  archforge db restore f.sql    restore a backup (asks YES)",
                "",
                "Docs: archforge-cli/README.md — command reference and password resolution rules."
        },
        subcommands = {
                InitCommand.class,
                DevCommand.class,
                UpCommand.class,
                DownCommand.class,
                StatusCommand.class,
                LogsCommand.class,
                RestartCommand.class,
                DoctorCommand.class,
                BuildCommand.class,
                DbCommand.class,
                InfraCommand.class,
                SkillsCommand.class,
                McpCommand.class,
                NewCommand.class,
                ModuleCommand.class,
                MetaCommand.class,
                AutoComplete.GenerateCompletion.class,
                HelpCommand.class
        })
public class ArchForgeCli implements Runnable {

    @Option(names = "--mcp", description = "Start MCP stdio server (same as `archforge mcp`)")
    boolean mcp;

    @Option(
            names = {
                    "--verbose"
            },
            scope = CommandLine.ScopeType.INHERIT,
            description = "Print every external command before running it")
    void setVerbose(boolean verbose) {
        ProcessRunner.setVerbose(verbose);
    }

    @Override
    public void run() {
        if (mcp) {
            try {
                McpServerMode.run();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return;
        }
        CommandLine.usage(this, System.out);
    }

    public static void main(String[] args) {
        int code = new CommandLine(new ArchForgeCli())
                .setCommandName("archforge")
                .execute(args);
        System.exit(code);
    }

    /** Reads the version baked into the jar manifest by the shadowJar build. */
    public static class ManifestVersionProvider implements CommandLine.IVersionProvider {
        @Override
        public String[] getVersion() {
            String version = ArchForgeCli.class.getPackage().getImplementationVersion();
            return new String[] {
                    "archforge-cli " + (version == null ? "dev" : version)
            };
        }
    }
}
