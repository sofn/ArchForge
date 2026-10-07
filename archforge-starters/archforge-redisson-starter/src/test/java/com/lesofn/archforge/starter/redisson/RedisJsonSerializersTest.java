package com.lesofn.archforge.starter.redisson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import tools.jackson.databind.DatabindException;

/**
 * The cache/Redis value serializer trusts the {@code @class} marker in the stored JSON, so whoever can
 * write to Redis picks which class gets instantiated. It must therefore only resolve an allow-list.
 */
class RedisJsonSerializersTest {

    private final GenericJacksonJsonRedisSerializer serializer = RedisJsonSerializers.safeJson(List.of());

    public static class Sample {
        public String name = "n";
        public List<Long> ids = new ArrayList<>(List.of(1L, 2L));
        public Map<String, Object> extra = new LinkedHashMap<>();
    }

    @Test
    void roundTripsTheValueShapesTheProjectActuallyCaches() {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("text", "hello");
        value.put("count", 3);
        value.put("big", 9_000_000_000L);
        value.put("price", new BigDecimal("12.50"));
        value.put("flag", true);
        value.put("when", LocalDateTime.of(2026, 10, 7, 12, 30));
        value.put("tags", new ArrayList<>(List.of("a", "b")));
        value.put("nested", new LinkedHashMap<>(Map.of("k", "v")));
        value.put("id", UUID.fromString("00000000-0000-0000-0000-000000000001"));

        Object back = serializer.deserialize(serializer.serialize(value));

        Map<?, ?> map = assertInstanceOf(Map.class, back);
        assertEquals("hello", map.get("text"));
        assertEquals(3, map.get("count"));
        assertEquals(new ArrayList<>(List.of("a", "b")), map.get("tags"));
        assertEquals(Map.of("k", "v"), map.get("nested"));
        assertNotNull(map.get("when"));
        assertNotNull(map.get("price"));
    }

    @Test
    void roundTripsProjectTypes() {
        Sample sample = new Sample();
        sample.extra.put("x", 1);

        Object back = serializer.deserialize(serializer.serialize(sample));

        Sample restored = assertInstanceOf(Sample.class, back);
        assertEquals("n", restored.name);
        assertEquals(List.of(1L, 2L), restored.ids);
    }

    @Test
    void refusesWellKnownGadgetClassNames() {
        for (String gadget : List.of("javax.naming.InitialContext", "com.sun.rowset.JdbcRowSetImpl",
                "org.springframework.context.support.ClassPathXmlApplicationContext", "java.lang.ProcessBuilder")) {
            byte[] forged = ("{\"@class\":\"" + gadget + "\"}").getBytes(StandardCharsets.UTF_8);

            SerializationException e = assertThrows(SerializationException.class, () -> serializer.deserialize(forged),
                    gadget);

            // refused by our allow-list (InvalidTypeIdException) or by Jackson's own gadget deny-list (InvalidDefinitionException)
            assertTrue(rootCause(e) instanceof DatabindException, gadget + " -> " + rootCause(e));
        }
    }

    @Test
    void extraPrefixesWidenTheAllowListExplicitly() {
        // javax.naming.InitialContext (a JNDI entry point) stands in for "some class outside the allow-list"
        byte[] stored = "{\"@class\":\"javax.naming.InitialContext\"}".getBytes(StandardCharsets.UTF_8);
        assertThrows(SerializationException.class, () -> serializer.deserialize(stored));

        GenericJacksonJsonRedisSerializer widened = RedisJsonSerializers.safeJson(List.of("javax.naming.InitialContext"));

        assertInstanceOf(javax.naming.InitialContext.class, widened.deserialize(stored));
    }

    private static Throwable rootCause(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }
}
