package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.mcp.McpServerMode;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(
        mixinStandardHelpOptions = true,
        name = "mcp",
        description = "Run the MCP stdio server (editor/AI tool integration)")
public class McpCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        try {
            McpServerMode.run();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return 0;
    }
}
