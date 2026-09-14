package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.proc.DevStack;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(
        mixinStandardHelpOptions = true,
        name = "dev",
        description = "Start local dev stack: infra containers + detached bootRun/pnpm processes (logs under logs/)")
public class DevCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        return DevStack.start(ProjectPaths.repoRoot());
    }
}
