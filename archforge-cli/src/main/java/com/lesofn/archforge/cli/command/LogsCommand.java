package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.Profile;
import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.docker.ComposeSupport;
import com.lesofn.archforge.cli.proc.DevStack;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        mixinStandardHelpOptions = true,
        name = "logs",
        description = "Tail dev logs (logs/*.log) or infra container logs (--infra)")
public class LogsCommand implements Callable<Integer> {

    @Parameters(index = "0", arity = "0..1", description = "Service name filter (e.g. server-admin)")
    @Nullable
    String service;

    @Option(names = {
            "-f", "--follow"
    }, description = "Follow output (tail -f)")
    boolean follow;

    @Option(names = "--infra", description = "Show infra container logs instead of dev logs")
    boolean infra;

    @Option(
            names = {
                    "-p", "--profile"
            },
            defaultValue = "dev",
            converter = Profile.Converter.class,
            description = "Stack profile for --stack logs: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})")
    Profile profile;

    @Option(names = "--stack", description = "Show compose stack logs instead of dev logs")
    boolean stack;

    @Override
    public Integer call() throws IOException {
        Path root = ProjectPaths.repoRoot();
        ComposeSupport compose = new ComposeSupport(new ProcessRunner(), root);
        if (infra) {
            return compose.logsInfra(follow);
        }
        if (stack) {
            return new ProcessRunner()
                    .run(
                            List.of(
                                    "docker", "compose", "-f",
                                    compose.stackFile(profile).toString(), "logs", "--tail", "100"),
                            ProjectPaths.dockerDir(root),
                            Map.of(),
                            true);
        }
        Path logs = DevStack.logsDir(root);
        if (!Files.isDirectory(logs)) {
            System.out.println("No logs/ directory yet — `archforge dev` first.");
            return 0;
        }
        List<String> files = new ArrayList<>();
        try (Stream<Path> stream = Files.list(logs)) {
            stream.filter(p -> String.valueOf(p.getFileName()).endsWith(".log"))
                    .filter(p -> service == null || String.valueOf(p.getFileName()).startsWith(service))
                    .forEach(p -> files.add(p.toString()));
        }
        if (files.isEmpty()) {
            System.out.println("No matching log files under " + logs);
            return 0;
        }
        List<String> command = new ArrayList<>(List.of("tail"));
        if (follow) {
            command.add("-F");
        }
        command.add("-n");
        command.add("50");
        command.addAll(files);
        return new ProcessRunner().run(command, root, Map.of(), true);
    }
}
