package com.lesofn.archforge.server.admin.service.scheduler;

import com.github.kagkarlsson.scheduler.exceptions.SerializationException;
import com.github.kagkarlsson.scheduler.serializer.Serializer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

/**
 * Java-serialization reader for {@code scheduled_tasks.task_data} rows written
 * before the switch to JSON. {@code ObjectInputStream.resolveClass} resolves
 * names against whatever loader the worker thread happens to see — under
 * DevTools restarts that yields a different {@code JobInvocationData} class
 * than the cast site's, hence {@code ClassCastException: cannot cast X to X}.
 * Pinning resolution to the loader that loaded {@link JobInvocationData} (the
 * current restart loader) makes every legacy row readable again.
 *
 * <p>
 * Only ever used as the fallback deserializer inside {@code
 * SerializerWithFallbackDeserializers} — new writes are JSON and never reach
 * {@link #serialize}.
 */
final class PinnedLoaderJavaDeserializer implements Serializer {

    private final ClassLoader classLoader = JobInvocationData.class.getClassLoader();

    @Override
    public byte[] serialize(Object data) {
        try (var bytes = new ByteArrayOutputStream(); var out = new ObjectOutputStream(bytes)) {
            out.writeObject(data);
            out.flush();
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new SerializationException("Failed to serialize object", e);
        }
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] serializedData) {
        try (var in = new ObjectInputStream(new ByteArrayInputStream(serializedData)) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                return Class.forName(desc.getName(), false, classLoader);
            }
        }) {
            return clazz.cast(in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new SerializationException("Failed to deserialize object", e);
        }
    }
}
