package com.lesofn.archforge.server.admin.service.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import org.junit.jupiter.api.Test;

class LlmClientFactoryTest {

    @Test
    void openAiCompatibleUrlUsesChatCompletions() {
        ArchForgeProperties.Llm llm = new ArchForgeProperties.Llm();
        llm.setProvider("openai");
        llm.setBaseUrl("https://api.openai.com/v1");
        assertEquals("https://api.openai.com/v1/chat/completions", LlmClientFactory.chatUrl(llm));
    }

    @Test
    void anthropicUrlUsesMessages() {
        ArchForgeProperties.Llm llm = new ArchForgeProperties.Llm();
        llm.setProvider("anthropic");
        llm.setBaseUrl("https://api.anthropic.com");
        assertEquals("https://api.anthropic.com/v1/messages", LlmClientFactory.chatUrl(llm));
    }

    @Test
    void missingApiKeyFailsFast() {
        ArchForgeProperties.Llm llm = new ArchForgeProperties.Llm();
        llm.setProvider("openai");
        llm.setApiKey("");
        assertThrows(ChatAiException.class, () -> LlmClientFactory.requireConfigured(llm));
    }
}
