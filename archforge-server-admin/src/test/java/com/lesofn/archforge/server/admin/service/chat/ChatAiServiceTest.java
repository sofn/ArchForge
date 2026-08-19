package com.lesofn.archforge.server.admin.service.chat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import org.junit.jupiter.api.Test;

class ChatAiServiceTest {

    @Test
    void configStatusReportsUnconfiguredWhenApiKeyBlank() {
        ArchForgeProperties properties = new ArchForgeProperties();
        properties.getLlm().setApiKey("");
        ChatAiService service = new ChatAiService(properties);
        assertFalse((Boolean) service.configStatus().get("configured"));
    }

}
