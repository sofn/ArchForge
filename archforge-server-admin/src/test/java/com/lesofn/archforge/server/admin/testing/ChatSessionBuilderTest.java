package com.lesofn.archforge.server.admin.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Verifies the chat session message structures match ChatAiService expectations. */
class ChatSessionBuilderTest {

    @Test
    void buildsUserAndAssistantMessages() {
        List<Map<String, String>> messages = ChatSessionTestBuilder.aSession()
                .withUserMessage("你好")
                .withAssistantMessage("您好，有什么可以帮您？")
                .buildMessages();

        assertEquals(2, messages.size());
        assertEquals("user", messages.get(0).get("role"));
        assertEquals("你好", messages.get(0).get("content"));
        assertEquals("assistant", messages.get(1).get("role"));
    }

    @Test
    void sessionIdsAreUniqueByDefault() {
        String first = ChatSessionTestBuilder.aSession().sessionId();
        String second = ChatSessionTestBuilder.aSession().sessionId();

        assertNotEquals(first, second);
    }

    @Test
    void explicitSessionIdIsPreserved() {
        String id = ChatSessionTestBuilder.aSession().withSessionId("fixed-id").sessionId();

        assertEquals("fixed-id", id);
    }

    @Test
    void buildMessagesReturnsDefensiveCopy() {
        ChatSessionTestBuilder builder = ChatSessionTestBuilder.aSession().withUserMessage("hi");
        List<Map<String, String>> first = builder.buildMessages();
        first.clear();

        assertEquals(1, builder.buildMessages().size());
        assertDistinctInstances(first, builder.buildMessages());
    }

    @SuppressWarnings("ReferenceEquality") // 断言的是"每次返回新实例"这一引用语义本身
    private static void assertDistinctInstances(Object a, Object b) {
        assertTrue(a != b);
    }
}
