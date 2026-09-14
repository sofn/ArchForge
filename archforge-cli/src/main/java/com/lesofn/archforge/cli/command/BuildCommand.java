package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        mixinStandardHelpOptions = true,
        name = "build",
        description = "Build backend bootBuildImage + frontend docker images (streams output)")
public class BuildCommand implements Callable<Integer> {

    @Option(names = {
            "-p", "--profile"
    }, defaultValue = "dev", description = "Image tag suffix")
    String profile;

    @Override
    public Integer call() {
        Path root = ProjectPaths.repoRoot();
        ProcessRunner runner = new ProcessRunner();
        int admin = runner.run(
                List.of("./gradlew", ":archforge-server-admin:bootBuildImage", "-x", "test"),
                root, Map.of(), true);
        if (admin != 0) {
            return admin;
        }
        int web = runner.run(
                List.of("./gradlew", ":archforge-server-web:bootBuildImage", "-x", "test"),
                root, Map.of(), true);
        if (web != 0) {
            return web;
        }
        Path frontendDockerfile = root.resolve("docker/Dockerfile.frontend");
        if (Files.exists(frontendDockerfile)) {
            Path adminRepo = ProjectPaths.adminRepo(root);
            Path webRepo = ProjectPaths.webRepo(root);
            if (Files.exists(adminRepo)) {
                int code = runner.run(
                        List.of("docker", "build", "-f", frontendDockerfile.toString(), "-t", "archforge-admin:" + profile,
                                "."),
                        adminRepo, Map.of(), true);
                if (code != 0) {
                    return code;
                }
            }
            if (Files.exists(webRepo)) {
                int code = runner.run(
                        List.of("docker", "build", "-f", frontendDockerfile.toString(), "-t", "archforge-web:" + profile, "."),
                        webRepo, Map.of(), true);
                if (code != 0) {
                    return code;
                }
            }
        }
        return 0;
    }
}
