package com.lesofn.archforge.infrastructure.dictionary;

import com.lesofn.archforge.common.enums.BasicEnum;
import com.lesofn.archforge.common.enums.DictionaryEnum;
import com.lesofn.archforge.common.enums.dictionary.Dictionary;
import com.lesofn.archforge.common.enums.dictionary.DictionaryData;
import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnumDictionaryRegistry {

    private final ArchForgeProperties config;

    private final Map<String, EnumDictionary> byCode = new LinkedHashMap<>();
    private final Map<Long, EnumDictionary> byTypeId = new HashMap<>();
    private final Map<Long, EnumDictionaryItem> byItemId = new HashMap<>();

    @PostConstruct
    public void init() {
        if (!config.getDictionary().isEnabled()) {
            log.info("Enum dictionary scan is disabled");
            return;
        }
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        CachingMetadataReaderFactory factory = new CachingMetadataReaderFactory(resolver);
        ClassLoader classLoader = this.getClass().getClassLoader();
        for (String basePackage : config.getDictionary().getEnumBasePackages()) {
            scanPackage(basePackage, resolver, factory, classLoader);
        }
        factory.clearCache();
        log.info("Loaded {} enum dictionaries", byCode.size());
    }

    private static final String ENUM_SUPER_CLASS_NAME = "java.lang.Enum";

    private void scanPackage(String basePackage, PathMatchingResourcePatternResolver resolver,
            CachingMetadataReaderFactory factory, ClassLoader classLoader) {
        String packageSearchPath = "classpath*:" + basePackage.replace('.', '/') + "/**/*.class";
        try {
            Resource[] resources = resolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                if (!resource.isReadable()) {
                    continue;
                }
                MetadataReader reader = factory.getMetadataReader(resource);
                if (reader.getClassMetadata().isInterface() || reader.getClassMetadata().isAnnotation()) {
                    continue;
                }
                if (!ENUM_SUPER_CLASS_NAME.equals(reader.getClassMetadata().getSuperClassName())) {
                    continue;
                }
                String className = reader.getClassMetadata().getClassName();
                try {
                    Class<?> clazz = Class.forName(className, false, classLoader);
                    loadIfDictionary(clazz);
                } catch (ClassNotFoundException | LinkageError e) {
                    log.warn("Failed to load class {}", className, e);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan enum dictionaries in package " + basePackage, e);
        }
    }

    @SuppressWarnings("rawtypes")
    private void loadIfDictionary(Class<?> clazz) {
        if (!clazz.isEnum() || !BasicEnum.class.isAssignableFrom(clazz)) {
            return;
        }
        Dictionary annotation = clazz.getAnnotation(Dictionary.class);
        if (annotation == null) {
            return;
        }
        String dictCode = annotation.name();
        if (!StringUtils.hasText(dictCode)) {
            throw new IllegalStateException("@Dictionary 'name' must not be blank on " + clazz.getName());
        }
        if (byCode.containsKey(dictCode)) {
            throw new IllegalStateException("Duplicate enum dictionary code: " + dictCode);
        }
        BasicEnum[] constants = (BasicEnum[]) clazz.getEnumConstants();
        if (constants == null || constants.length == 0) {
            return;
        }
        Long dictTypeId = syntheticId(dictCode);
        List<EnumDictionaryItem> items = new ArrayList<>(constants.length);
        for (int i = 0; i < constants.length; i++) {
            BasicEnum constant = constants[i];
            String itemCode = String.valueOf(constant.getValue());
            String itemLabel = constant.getDescription();
            String cssTag = constant instanceof DictionaryEnum d ? d.getCssTag() : null;
            Long itemId = syntheticItemId(dictCode, itemCode);
            EnumDictionaryItem item = new EnumDictionaryItem(dictTypeId, itemId, itemCode, itemLabel, i, 1, cssTag);
            items.add(item);
            byItemId.put(itemId, item);
        }
        String dictName = StringUtils.hasText(annotation.label()) ? annotation.label() : dictCode;
        EnumDictionary dictionary = new EnumDictionary(dictTypeId, dictCode, dictName, annotation.description(), 1, 0, items);
        byCode.put(dictCode, dictionary);
        byTypeId.put(dictTypeId, dictionary);
    }

    private static final long TYPE_ID_MASK = (1L << 62) - 1;
    private static final long ITEM_ID_SPACE_OFFSET = 1L << 62;

    private static long hash64(String input) {
        long h = 0xcbf29ce484222325L;
        for (int i = 0; i < input.length(); i++) {
            h ^= input.charAt(i);
            h *= 0x100000001b3L;
        }
        return h;
    }

    private Long syntheticId(String dictCode) {
        return Long.MIN_VALUE + (hash64(dictCode) & TYPE_ID_MASK);
    }

    private Long syntheticItemId(String dictCode, String itemCode) {
        return Long.MIN_VALUE + ITEM_ID_SPACE_OFFSET + (hash64(dictCode + "\0" + itemCode) & TYPE_ID_MASK);
    }

    public Optional<EnumDictionary> findByCode(String dictCode) {
        return Optional.ofNullable(byCode.get(dictCode));
    }

    public Optional<EnumDictionary> findByTypeId(Long dictTypeId) {
        return Optional.ofNullable(byTypeId.get(dictTypeId));
    }

    public Optional<EnumDictionaryItem> findItemById(Long dictItemId) {
        return Optional.ofNullable(byItemId.get(dictItemId));
    }

    public List<EnumDictionary> findAll() {
        return List.copyOf(byCode.values());
    }

    public boolean isEnumDictCode(String dictCode) {
        return byCode.containsKey(dictCode);
    }

    public boolean isEnumDictTypeId(Long dictTypeId) {
        return byTypeId.containsKey(dictTypeId);
    }

    public boolean isEnumDictItemId(Long dictItemId) {
        return byItemId.containsKey(dictItemId);
    }

    public Map<String, List<DictionaryData>> asDictionaryDataMap() {
        Map<String, List<DictionaryData>> result = byCode.values().stream()
                .collect(Collectors.toMap(
                        EnumDictionary::getDictCode,
                        d -> d.getItems().stream()
                                .map(i -> new DictionaryData(i.getLabel(), Integer.parseInt(i.getCode()), i.getCssTag()))
                                .toList(),
                        (a, b) -> a,
                        LinkedHashMap::new));
        return Map.copyOf(result);
    }
}
