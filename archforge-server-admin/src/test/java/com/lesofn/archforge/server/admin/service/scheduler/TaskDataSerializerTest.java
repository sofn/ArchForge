package com.lesofn.archforge.server.admin.service.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.kagkarlsson.scheduler.serializer.JavaSerializer;
import com.github.kagkarlsson.scheduler.serializer.JacksonSerializer;
import com.github.kagkarlsson.scheduler.serializer.Serializer;
import com.github.kagkarlsson.scheduler.serializer.SerializerWithFallbackDeserializers;
import com.github.kagkarlsson.scheduler.task.schedule.CronSchedule;
import com.github.kagkarlsson.scheduler.task.schedule.Schedules;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * task_data must serialize as JSON (classloader-immune) while legacy
 * Java-serialized rows stay readable via the pinned-loader fallback — the
 * self-healing migration path for rows written before the switch.
 */
class TaskDataSerializerTest {

    private final Serializer serializer = new SerializerWithFallbackDeserializers(new JacksonSerializer(), new PinnedLoaderJavaDeserializer());

    private static JobInvocationData sample() {
        return new JobInvocationData(7L, "demo", "grp", "myBean", "run", "a=1", Schedules.cron("0 0 * * * *"));
    }

    @Test
    void newWritesAreJsonAndRoundTrip() {
        byte[] bytes = serializer.serialize(sample());
        assertEquals('{', new String(bytes, StandardCharsets.UTF_8).charAt(0));

        JobInvocationData back = serializer.deserialize(JobInvocationData.class, bytes);
        assertEquals(sample(), back);
        assertInstanceOf(CronSchedule.class, back.schedule());
    }

    @Test
    void legacyJavaSerializedRowsStillDeserialize() {
        byte[] legacy = new JavaSerializer().serialize(sample());
        assertTrue(legacy[0] == (byte) 0xAC && legacy[1] == (byte) 0xED, "expect java serialization magic");

        JobInvocationData back = serializer.deserialize(JobInvocationData.class, legacy);
        assertEquals(sample(), back);
    }
}
