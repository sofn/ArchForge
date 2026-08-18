package com.lesofn.archforge.cli.mcp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Minimal stdio JSON-RPC MCP server exposing Phase 1 tools.
 */
public final class McpServerMode {

    static final List<String> TOOLS = List.of(
            "skill_list", "skill_install", "project_info", "up", "down", "db_backup", "db_recovery");

    private McpServerMode() {
    }

    public static void run() throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("\"method\":\"tools/list\"") || line.contains("tools/list")) {
                System.out.println(toolsListResponse());
            } else if (line.contains("initialize")) {
                System.out.println(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{\"protocolVersion\":\"2024-11-05\",\"capabilities\":{\"tools\":{}},\"serverInfo\":{\"name\":\"archforge-cli\",\"version\":\"0.1.0\"}}}");
            } else {
                System.out.println(
                        "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32601,\"message\":\"Method not implemented in Phase 1\"}}");
            }
            System.out.flush();
        }
    }

    static String toolsListResponse() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{\"tools\":[");
        for (int i = 0; i < TOOLS.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append("{\"name\":\"").append(TOOLS.get(i)).append("\",\"description\":\"")
                    .append(TOOLS.get(i))
                    .append("\"}");
        }
        builder.append("]}}");
        return builder.toString();
    }
}
