package com.lesofn.archforge.server.admin.testing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test data builder for chat session structures used by {@code ChatAiService}. Sessions are
 * in-memory {@code List<Map<String, String>>} message histories keyed by "role"/"content".
 */
public final class ChatSessionTestBuilder {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private final List<Map<String, String>> messages = new ArrayList<>();
    private String sessionId;

    private ChatSessionTestBuilder() {
    }

    public static ChatSessionTestBuilder aSession() {
        return new ChatSessionTestBuilder();
    }

    public ChatSessionTestBuilder withSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public ChatSessionTestBuilder withUserMessage(String content) {
        messages.add(message("user", content));
        return this;
    }

    public ChatSessionTestBuilder withAssistantMessage(String content) {
        messages.add(message("assistant", content));
        return this;
    }

    /** A ready-to-use session id; unique per call so tests never collide. */
    public String sessionId() {
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        }
        return sessionId;
    }

    public List<Map<String, String>> buildMessages() {
        return new ArrayList<>(messages);
    }

    /** Number of sessions created so far, useful for asserting list sizes. */
    public static int sequence() {
        return Math.toIntExact(SEQUENCE.incrementAndGet());
    }

    private static Map<String, String> message(String role, String content) {
        Map<String, String> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }
}
