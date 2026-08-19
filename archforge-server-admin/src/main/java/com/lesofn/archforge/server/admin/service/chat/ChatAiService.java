package com.lesofn.archforge.server.admin.service.chat;

import com.lesofn.archforge.common.utils.jackson.JsonUtil;
import com.lesofn.archforge.infrastructure.auth.LoginContext;
import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ChatAiService {

    private final ArchForgeProperties properties;
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, List<Map<String, String>>>> sessions = new ConcurrentHashMap<>();

    public Map<String, Object> configStatus() {
        ArchForgeProperties.Llm llm = properties.getLlm();
        return Map.of(
                "provider", llm.getProvider() == null ? "openai" : llm.getProvider(),
                "model", llm.getModel() == null ? "" : llm.getModel(),
                "baseUrl", llm.getBaseUrl() == null ? "" : llm.getBaseUrl(),
                "configured", llm.getApiKey() != null && !llm.getApiKey().isBlank());
    }

    public Map<String, Object> createSession() {
        String id = UUID.randomUUID().toString();
        userSessions().put(id, new ArrayList<>());
        return Map.of("id", id, "messages", List.of());
    }

    public List<Map<String, Object>> listSessions() {
        return userSessions().keySet().stream().map(id -> Map.<String, Object> of("id", id)).toList();
    }

    public List<Map<String, String>> messages(String sessionId) {
        List<Map<String, String>> history = userSessions().get(sessionId);
        if (history == null) {
            throw new ChatAiException(ChatAiErrorCode.CHAT_SESSION_NOT_FOUND);
        }
        return history;
    }

    public void deleteSession(String sessionId) {
        if (userSessions().remove(sessionId) == null) {
            throw new ChatAiException(ChatAiErrorCode.CHAT_SESSION_NOT_FOUND);
        }
    }

    public SseEmitter stream(String sessionId, String content) {
        ArchForgeProperties.Llm llm = LlmClientFactory.requireConfigured(properties.getLlm());
        List<Map<String, String>> history = userSessions().get(sessionId);
        if (history == null) {
            throw new ChatAiException(ChatAiErrorCode.CHAT_SESSION_NOT_FOUND);
        }
        history.add(Map.of("role", "user", "content", content));
        SseEmitter emitter = new SseEmitter(120_000L);
        Thread.ofVirtual().start(() -> complete(emitter, llm, sessionId, history));
        return emitter;
    }

    private ConcurrentHashMap<String, List<Map<String, String>>> userSessions() {
        return sessions.computeIfAbsent(LoginContext.getAdminUserId(), key -> new ConcurrentHashMap<>());
    }

    private void complete(
            SseEmitter emitter, ArchForgeProperties.Llm llm, String sessionId, List<Map<String, String>> history) {
        try {
            String reply = callModel(llm, history);
            history.add(Map.of("role", "assistant", "content", reply));
            userSessions().put(sessionId, history);
            emitter.send(SseEmitter.event().name("delta").data(reply, MediaType.TEXT_PLAIN));
            emitter.send(SseEmitter.event().name("done").data("[DONE]"));
            emitter.complete();
        } catch (Exception e) {
            try {
                emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
            } catch (Exception ignored) {
                // already closing
            }
            emitter.completeWithError(e);
        }
    }

    String callModel(ArchForgeProperties.Llm llm, List<Map<String, String>> history) throws Exception {
        String provider = llm.getProvider() == null ? "openai" : llm.getProvider().toLowerCase();
        String body = "anthropic".equals(provider) ? anthropicBody(llm, history) : openAiBody(llm, history);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(LlmClientFactory.chatUrl(llm)))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        if ("anthropic".equals(provider)) {
            builder.header("x-api-key", llm.getApiKey());
            builder.header("anthropic-version", llm.getAnthropicVersion());
        } else {
            builder.header("Authorization", "Bearer " + llm.getApiKey());
        }
        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new ChatAiException(ChatAiErrorCode.LLM_REQUEST_FAILED);
        }
        JsonNode root = objectMapper.readTree(response.body());
        if ("anthropic".equals(provider)) {
            return root.path("content").path(0).path("text").asString("");
        }
        return root.path("choices").path(0).path("message").path("content").asString("");
    }

    private String openAiBody(ArchForgeProperties.Llm llm, List<Map<String, String>> history) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", llm.getModel());
        payload.put("messages", history);
        payload.put("stream", false);
        return objectMapper.writeValueAsString(payload);
    }

    private String anthropicBody(ArchForgeProperties.Llm llm, List<Map<String, String>> history) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", llm.getModel());
        payload.put("max_tokens", 1024);
        payload.put("messages", history);
        return objectMapper.writeValueAsString(payload);
    }
}
