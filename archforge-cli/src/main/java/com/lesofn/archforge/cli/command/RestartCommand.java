package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.proc.DevStack;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(
        mixinStandardHelpOptions = true,
        name = "restart",
        description = "Restart the dev stack: kill dev processes via run/*.pid, then start again")
public class RestartCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        Path root = ProjectPaths.repoRoot();
        DevStack.stop(root);
        return DevStack.start(root);
    }
}
