package com.lesofn.archforge.server.admin.service.chat;

import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;

public final class LlmClientFactory {

    private LlmClientFactory() {
    }

    public static String chatUrl(ArchForgeProperties.Llm llm) {
        String base = llm.getBaseUrl() == null ? "" : llm.getBaseUrl().replaceAll("/+$", "");
        String provider = llm.getProvider() == null ? "openai" : llm.getProvider().toLowerCase();
        if ("anthropic".equals(provider)) {
            return base.endsWith("/v1/messages") ? base : base + "/v1/messages";
        }
        return base.endsWith("/chat/completions") ? base : base + "/chat/completions";
    }

    public static ArchForgeProperties.Llm requireConfigured(ArchForgeProperties.Llm llm) {
        if (llm == null || llm.getApiKey() == null || llm.getApiKey().isBlank()) {
            throw new IllegalStateException("arch-forge.llm.api-key is required (set an OpenAI or Anthropic compatible key)");
        }
        return llm;
    }
}
