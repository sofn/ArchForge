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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatAiService {

    private static final int MAX_SESSION_USERS = 1000;

    private final ArchForgeProperties properties;
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final Map<Long, Map<String, List<Map<String, String>>>> sessions = Collections.synchronizedMap(
            new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(
                        Map.Entry<Long, Map<String, List<Map<String, String>>>> eldest) {
                    return size() > MAX_SESSION_USERS;
                }
            });

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
        currentUserSessions().put(id, new CopyOnWriteArrayList<>());
        return Map.of("id", id, "messages", List.of());
    }

    public List<Map<String, Object>> listSessions() {
        Map<String, List<Map<String, String>>> userSessions = currentUserSessions();
        synchronized (userSessions) {
            return userSessions.keySet().stream().map(id -> Map.<String, Object> of("id", id)).toList();
        }
    }

    public List<Map<String, String>> messages(String sessionId) {
        List<Map<String, String>> history = currentUserSessions().get(sessionId);
        if (history == null) {
            throw new ChatAiException(ChatAiErrorCode.CHAT_SESSION_NOT_FOUND);
        }
        return history;
    }

    public void deleteSession(String sessionId) {
        if (currentUserSessions().remove(sessionId) == null) {
            throw new ChatAiException(ChatAiErrorCode.CHAT_SESSION_NOT_FOUND);
        }
    }

    public SseEmitter stream(String sessionId, String content) {
        ArchForgeProperties.Llm llm = LlmClientFactory.requireConfigured(properties.getLlm());
        List<Map<String, String>> history = currentUserSessions().get(sessionId);
        if (history == null) {
            throw new ChatAiException(ChatAiErrorCode.CHAT_SESSION_NOT_FOUND);
        }
        history.add(Map.of("role", "user", "content", content));
        SseEmitter emitter = new SseEmitter(120_000L);
        Thread.ofVirtual().start(() -> complete(emitter, llm, history));
        return emitter;
    }

    private Map<String, List<Map<String, String>>> currentUserSessions() {
        return userSessions(LoginContext.getAdminUserId());
    }

    private Map<String, List<Map<String, String>>> userSessions(Long userId) {
        return sessions.computeIfAbsent(userId, key -> new ConcurrentHashMap<>());
    }

    private void complete(SseEmitter emitter, ArchForgeProperties.Llm llm, List<Map<String, String>> history) {
        try {
            String reply = callModel(llm, history);
            history.add(Map.of("role", "assistant", "content", reply));
            emitter.send(SseEmitter.event().name("delta").data(reply, MediaType.TEXT_PLAIN));
            emitter.send(SseEmitter.event().name("done").data("[DONE]"));
            emitter.complete();
        } catch (Exception e) {
            log.error("[ChatAI] streaming failed", e);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(ChatAiErrorCode.LLM_REQUEST_FAILED.getMsg(), MediaType.TEXT_PLAIN));
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
