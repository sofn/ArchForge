package com.lesofn.archforge.meta.table.api.definition;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * {@link DefinitionSource} backed by {@code classpath*:<base>/*.yaml}
 * resources — the packaged mirror of the definition directory (e.g.
 * {@code archforge/meta} inside the jar). The same file name on two classpath
 * roots is ambiguous and fails instead of letting one silently win.
 */
public class ClasspathDefinitionSource implements DefinitionSource {

    private final String basePath;
    private final ClassLoader classLoader;

    public ClasspathDefinitionSource(String basePath, ClassLoader classLoader) {
        this.basePath = basePath;
        this.classLoader = classLoader;
    }

    @Override
    public Map<String, String> load() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        Map<String, Resource> byName = new TreeMap<>();
        Map<String, String> out = new LinkedHashMap<>();
        try {
            for (Resource resource : resolver.getResources(pattern())) {
                String name = Objects.requireNonNull(resource.getFilename());
                Resource prior = byName.putIfAbsent(name, resource);
                if (prior != null) {
                    throw new IllegalStateException("duplicate definition file " + name + " on the classpath: " +
                            prior.getDescription() + " and " + resource.getDescription());
                }
            }
            for (Map.Entry<String, Resource> entry : byName.entrySet()) {
                out.put(entry.getKey(), entry.getValue().getContentAsString(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out;
    }

    private String pattern() {
        return "classpath*:" + basePath + "/*.yaml";
    }

    @Override
    public String toString() {
        return pattern();
    }
}
