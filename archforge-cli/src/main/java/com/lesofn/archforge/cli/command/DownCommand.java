package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.docker.ComposeSupport;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "down", description = "Stop the full stack")
public class DownCommand implements Callable<Integer> {

    @Option(names = "--profile", defaultValue = "dev")
    String profile;

    @Override
    public Integer call() {
        return new ComposeSupport(new ProcessRunner(), ProjectPaths.repoRoot()).stop(profile);
    }
}
