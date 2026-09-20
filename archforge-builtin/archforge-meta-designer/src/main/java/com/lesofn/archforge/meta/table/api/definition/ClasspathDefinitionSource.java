package com.lesofn.archforge.meta.table.api.definition;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * {@link DefinitionSource} backed by {@code classpath*:<base>/*.yaml}
 * resources — the packaged mirror of the definition directory (e.g.
 * {@code archforge/meta} inside the jar). Not wired anywhere yet; this is the
 * F2 fallback for deployed apps without a filesystem definition dir.
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
        var resolver = new PathMatchingResourcePatternResolver(classLoader);
        Map<String, String> out = new LinkedHashMap<>();
        try {
            Resource[] resources = resolver.getResources("classpath*:" + basePath + "/*.yaml");
            for (Resource resource : resources) {
                String name = Objects.requireNonNull(resource.getFilename());
                out.put(name, resource.getContentAsString(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out;
    }
}
