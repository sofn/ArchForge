package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.Profile;
import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.docker.ComposeSupport;
import com.lesofn.archforge.cli.proc.DevStack;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        mixinStandardHelpOptions = true,
        name = "status",
        description = "Show dev process liveness and compose container state")
public class StatusCommand implements Callable<Integer> {

    @Option(
            names = {
                    "-p", "--profile"
            },
            defaultValue = "dev",
            converter = Profile.Converter.class,
            description = "Stack profile for container status: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})")
    Profile profile;

    @Override
    public Integer call() {
        Path root = ProjectPaths.repoRoot();
        DevStack.status(root);
        ComposeSupport compose = new ComposeSupport(new ProcessRunner(), root);
        System.out.println();
        if (profile == Profile.dev && compose.stackFileExists(profile)) {
            // dev's stack file and the infra file are one compose project: two listings were the same containers twice
            System.out.println("Containers (dev — infra and app share one compose project):");
            return compose.psStack(profile);
        }
        System.out.println("Infra containers:");
        int code = compose.psInfra();
        if (compose.stackFileExists(profile)) {
            System.out.println();
            System.out.println("Stack containers (" + profile + "):");
            code = compose.psStack(profile);
        }
        return code;
    }
}
