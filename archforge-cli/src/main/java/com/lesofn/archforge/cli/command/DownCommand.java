package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.Profile;
import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.docker.ComposeSupport;
import com.lesofn.archforge.cli.proc.DevStack;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        mixinStandardHelpOptions = true,
        name = "down",
        description = "Stop everything: dev processes (run/*.pid) and the compose stack")
public class DownCommand implements Callable<Integer> {

    @Option(
            names = {
                    "-p", "--profile"
            },
            defaultValue = "dev",
            converter = Profile.Converter.class,
            description = "Stack profile: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})")
    Profile profile;

    @Option(
            names = {
                    "-v", "--volumes"
            },
            description = "Also remove named volumes — destroys local dev data")
    boolean volumes;

    @Override
    public Integer call() {
        DevStack.stop(ProjectPaths.repoRoot());
        ComposeSupport compose = new ComposeSupport(new ProcessRunner(), ProjectPaths.repoRoot());
        int code = compose.downStack(profile, volumes);
        // The infra deps are shared across both modes — stop them too.
        int infra = compose.downInfra(false);
        return code != 0 ? code : infra;
    }
}
