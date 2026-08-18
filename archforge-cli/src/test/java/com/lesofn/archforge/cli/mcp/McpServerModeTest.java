package com.lesofn.archforge.cli.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class McpServerModeTest {

    @Test
    void toolsListContainsSevenPhaseOneTools() {
        String json = McpServerMode.toolsListResponse();
        for (String tool : McpServerMode.TOOLS) {
            assertTrue(json.contains("\"" + tool + "\""), tool);
        }
        assertEquals(7, McpServerMode.TOOLS.size());
    }
}
