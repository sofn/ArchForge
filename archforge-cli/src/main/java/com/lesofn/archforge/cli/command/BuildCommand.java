package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
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

    @Option(
            names = "--allinone",
            description = "Build the all-in-one image (nginx + server-admin + server-web + next.js) " +
                    "instead of the per-service images")
    boolean allinone;

    @Override
    public Integer call() {
        Path root = ProjectPaths.repoRoot();
        ProcessRunner runner = new ProcessRunner();
        if (allinone) {
            return buildAllinone(root, runner);
        }
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

    /**
     * Stages pre-built artifacts into {@code docker/allinone/context/} then
     * builds {@code archforge:allinone}. The Dockerfile is a thin assembler —
     * all heavy lifting (bootJar, pnpm build) happens on the host so the image
     * build needs no frontend/backend toolchain inside Docker.
     */
    private int buildAllinone(Path root, ProcessRunner runner) {
        Path adminRepo = ProjectPaths.adminRepo(root);
        Path webRepo = ProjectPaths.webRepo(root);
        if (!Files.exists(adminRepo) || !Files.exists(webRepo)) {
            System.err.println("allinone needs sibling repos ArchForgeAdmin and ArchForgeWeb checked out next to ArchForge");
            return 1;
        }
        int jars = runner.run(
                List.of("./gradlew", ":archforge-server-admin:bootJar", ":archforge-server-web:bootJar", "-x", "test"),
                root, Map.of(), true);
        if (jars != 0) {
            return jars;
        }
        int code = pnpmBuild(runner, adminRepo);
        if (code != 0) {
            return code;
        }
        code = pnpmBuild(runner, webRepo);
        if (code != 0) {
            return code;
        }
        Path context = root.resolve("docker/allinone/context");
        try {
            stage(context, root, adminRepo, webRepo);
        } catch (UncheckedIOException e) {
            System.err.println("staging failed: " + e.getMessage());
            return 1;
        }
        return runner.run(
                List.of("docker", "build", "-t", "archforge:allinone", "."),
                root.resolve("docker/allinone"), Map.of(), true);
    }

    private int pnpmBuild(ProcessRunner runner, Path repo) {
        int code = runner.run(List.of("pnpm", "install", "--frozen-lockfile"), repo, Map.of(), true);
        if (code != 0) {
            return code;
        }
        return runner.run(List.of("pnpm", "build"), repo, Map.of(), true);
    }

    private void stage(Path context, Path root, Path adminRepo, Path webRepo) {
        Path adminDist = adminRepo.resolve("dist");
        Path webApp = webRepo.resolve("apps/web");
        Path standalone = webApp.resolve(".next/standalone");
        Path statik = webApp.resolve(".next/static");
        Path publik = webApp.resolve("public");
        Path adminJar = root.resolve("archforge-server-admin/build/libs/archforge-server-admin.jar");
        Path webJar = root.resolve("archforge-server-web/build/libs/archforge-server-web.jar");
        for (Path required : List.of(adminDist, standalone, statik, publik, adminJar, webJar)) {
            if (!Files.exists(required)) {
                throw new UncheckedIOException(new IOException("missing build artifact: " + required));
            }
        }
        deleteRecursively(context);
        copyTree(adminDist, context.resolve("admin"));
        copyTree(standalone, context.resolve("web"));
        copyTree(statik, context.resolve("web-static"));
        copyTree(publik, context.resolve("web-public"));
        copyFile(adminJar, context.resolve("server-admin.jar"));
        copyFile(webJar, context.resolve("server-web.jar"));
        System.out.println("staged allinone context at " + context);
    }

    private static void copyTree(Path from, Path to) {
        try (Stream<Path> walk = Files.walk(from)) {
            walk.forEach(src -> {
                Path dest = to.resolve(from.relativize(src).toString());
                try {
                    if (Files.isDirectory(src, LinkOption.NOFOLLOW_LINKS)) {
                        Files.createDirectories(dest);
                    } else {
                        Path parent = dest.getParent();
                        if (parent != null) {
                            Files.createDirectories(parent);
                        }
                        // NOFOLLOW_LINKS: pnpm's standalone output contains
                        // relative symlinks — copy the link, not the target.
                        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING, LinkOption.NOFOLLOW_LINKS);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void copyFile(Path from, Path to) {
        try {
            Path parent = to.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void deleteRecursively(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
