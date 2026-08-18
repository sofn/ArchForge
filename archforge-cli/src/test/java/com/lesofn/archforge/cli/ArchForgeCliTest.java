package com.lesofn.archforge.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class ArchForgeCliTest {

    @Test
    void helpListsCoreCommands() {
        StringWriter output = new StringWriter();
        CommandLine cmd = new CommandLine(new ArchForgeCli());
        cmd.setOut(new PrintWriter(output));
        int code = cmd.execute("--help");
        assertEquals(0, code);
        String text = output.toString();
        assertTrue(text.contains("init"));
        assertTrue(text.contains("infra"));
        assertTrue(text.contains("db"));
        assertTrue(text.contains("skills"));
    }
}
