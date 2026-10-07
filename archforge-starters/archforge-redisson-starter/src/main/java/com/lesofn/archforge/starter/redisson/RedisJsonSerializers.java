package com.lesofn.archforge.starter.redisson;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

/**
 * JSON value serializers for Redis whose polymorphic typing is restricted to an allow-list.
 *
 * <p>
 * {@code enableUnsafeDefaultTyping()} lets the stored JSON name any class through its {@code @class}
 * marker, so anyone able to write to Redis (shared instance, SSRF, missing {@code requirepass}) can have
 * the application instantiate a gadget class while reading the value back. Here only these may be
 * resolved: project types ({@code com.lesofn.archforge.}), collections, maps, numbers, strings, booleans,
 * {@code java.time} values, UUIDs, arrays and whatever the caller explicitly adds.
 */
public final class RedisJsonSerializers {

    private static final String PROJECT_PREFIX = "com.lesofn.archforge.";

    private RedisJsonSerializers() {
    }

    /**
     * Builds the allow-listed serializer used for Redis values and the L2 cache.
     *
     * @param extraAllowedPrefixes additional class-name prefixes (for example {@code "com.acme.order."}) whose
     *        types may be restored from Redis — application entities cached through the L2 cache
     */
    public static GenericJacksonJsonRedisSerializer safeJson(Collection<String> extraAllowedPrefixes) {
        return GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(typeValidator(extraAllowedPrefixes))
                .build();
    }

    static PolymorphicTypeValidator typeValidator(Collection<String> extraAllowedPrefixes) {
        BasicPolymorphicTypeValidator.Builder builder = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(PROJECT_PREFIX)
                .allowIfSubType(Collection.class)
                .allowIfSubType(Map.class)
                .allowIfSubType(Number.class)
                .allowIfSubType(CharSequence.class)
                .allowIfSubType(Boolean.class)
                .allowIfSubType(Temporal.class)
                .allowIfSubType(TemporalAmount.class)
                .allowIfSubType(UUID.class)
                .allowIfSubTypeIsArray();
        for (String prefix : extraAllowedPrefixes) {
            builder.allowIfSubType(prefix);
        }
        return builder.build();
    }
}
