package com.lesofn.archforge.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.cli.config.DbPasswordResolver;
import com.lesofn.archforge.cli.config.Profile;
import com.lesofn.archforge.cli.docker.ComposeSupport;
import com.lesofn.archforge.cli.proc.DevStack;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

class CliStructureTest {

    @TempDir
    Path tempDir;

    @Test
    void profileConverterAcceptsKnownValues() {
        Profile.Converter converter = new Profile.Converter();
        assertEquals(Profile.dev, converter.convert("dev"));
        assertEquals(Profile.nativeImage, converter.convert("native"));
        assertEquals(Profile.nativeImage, converter.convert("nativeImage"));
        assertEquals(Profile.prod, converter.convert("prod"));
    }

    @Test
    void profileConverterRejectsTypos() {
        Profile.Converter converter = new Profile.Converter();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> converter.convert("pod"));
        assertTrue(String.valueOf(e.getMessage()).contains("pod"));
    }

    @Test
    void stackFileMapsEveryProfile() {
        ComposeSupport compose = new ComposeSupport(new ProcessRunner(), tempDir);
        assertTrue(compose.stackFile(Profile.dev).endsWith("docker-compose.yml"));
        assertTrue(compose.stackFile(Profile.nativeImage).endsWith("docker-compose.native.yml"));
        assertTrue(compose.stackFile(Profile.staging).endsWith("docker-compose.staging.yml"));
        assertTrue(compose.stackFile(Profile.prod).endsWith("docker-compose.prod.yml"));
        assertTrue(compose.infraFile().endsWith("docker-compose.infra.yml"));
    }

    @Test
    void devStackDefinesFourServices() {
        // sibling frontend repos don't exist next to tempDir — only the two backend services
        List<DevStack.Service> services = DevStack.services(tempDir);
        assertEquals(2, services.size());
        assertEquals("server-admin", services.get(0).name());
        assertTrue(services.get(0).command().contains(":archforge-server-admin:bootRun"));
    }

    @Test
    void devStackStatusRunsWithoutPidFiles() {
        assertEquals(0, DevStack.status(tempDir));
        assertEquals(0, DevStack.stop(tempDir));
    }

    @Test
    void dbNameDefaultsAndEnvFile() throws Exception {
        assertEquals("archforge", DbPasswordResolver.resolveDbName(tempDir));
        Files.writeString(tempDir.resolve(".env"), "DB_NAME=customdb\n");
        assertEquals("customdb", DbPasswordResolver.resolveDbName(tempDir));
    }

    @Test
    void bareGroupCommandsPrintHelp() {
        StringWriterCapture out = new StringWriterCapture();
        CommandLine cmd = new CommandLine(new ArchForgeCli());
        cmd.setOut(out.writer());
        assertEquals(0, cmd.execute("db"));
        assertTrue(out.text().contains("restore"));
    }

    @Test
    void cleanMountedFilesRemovesBindMountDirs() throws Exception {
        Path dockerDir = tempDir.resolve("docker");
        for (String rel : List.of("logs", "logs-web", "allinone/context")) {
            Path dir = dockerDir.resolve(rel);
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("keep.log"), "x");
        }
        ComposeSupport compose = new ComposeSupport(new ProcessRunner(), tempDir);
        assertEquals(0, compose.cleanMountedFiles());
        for (String rel : List.of("logs", "logs-web", "allinone/context")) {
            assertTrue(Files.notExists(dockerDir.resolve(rel)), "still exists: " + rel);
        }
    }

    @Test
    void cleanMountedFilesIsNoopWhenNothingMounted() {
        ComposeSupport compose = new ComposeSupport(new ProcessRunner(), tempDir);
        assertEquals(0, compose.cleanMountedFiles());
    }

    @Test
    void infraCleanAbortsWithoutYes() {
        InputStream original = System.in;
        try {
            System.setIn(new ByteArrayInputStream("no\n".getBytes(StandardCharsets.UTF_8)));
            CommandLine cmd = new CommandLine(new ArchForgeCli());
            assertEquals(1, cmd.execute("infra", "clean"));
        } finally {
            System.setIn(original);
        }
    }

    @Test
    void helpShowsNewCommandTree() {
        StringWriterCapture out = new StringWriterCapture();
        CommandLine cmd = new CommandLine(new ArchForgeCli());
        cmd.setOut(out.writer());
        assertEquals(0, cmd.execute("--help"));
        for (String name : List.of("dev", "status", "doctor", "restart", "logs", "mcp", "completion", "help")) {
            assertTrue(out.text().contains(name), "missing " + name);
        }
    }

    private static final class StringWriterCapture {
        private final java.io.StringWriter buffer = new java.io.StringWriter();

        java.io.PrintWriter writer() {
            return new java.io.PrintWriter(buffer);
        }

        String text() {
            return buffer.toString();
        }
    }
}
