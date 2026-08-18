package com.lesofn.archforge.cli;

import com.lesofn.archforge.cli.command.BuildCommand;
import com.lesofn.archforge.cli.command.DbCommand;
import com.lesofn.archforge.cli.command.DockerCommand;
import com.lesofn.archforge.cli.command.DownCommand;
import com.lesofn.archforge.cli.command.InfraCommand;
import com.lesofn.archforge.cli.command.InitCommand;
import com.lesofn.archforge.cli.command.SkillsCommand;
import com.lesofn.archforge.cli.command.UpCommand;
import com.lesofn.archforge.cli.mcp.McpServerMode;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "archforge",
        mixinStandardHelpOptions = true,
        version = "archforge-cli 0.1.0",
        description = "ArchForge developer CLI",
        subcommands = {
                InitCommand.class,
                BuildCommand.class,
                UpCommand.class,
                DownCommand.class,
                DockerCommand.class,
                DbCommand.class,
                InfraCommand.class,
                SkillsCommand.class
        })
public class ArchForgeCli implements Runnable {

    @Option(names = "--mcp", description = "Start MCP stdio server")
    boolean mcp;

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
        int code = new CommandLine(new ArchForgeCli()).execute(args);
        System.exit(code);
    }
}
