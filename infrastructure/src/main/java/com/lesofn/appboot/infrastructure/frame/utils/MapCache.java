package com.lesofn.appboot.infrastructure.frame.utils;

import com.lesofn.appboot.common.enums.DictionaryEnum;
import com.lesofn.appboot.common.enums.dictionary.Dictionary;
import com.lesofn.appboot.common.enums.dictionary.DictionaryData;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 本地一级缓存  使用Map
 *
 * @author sofn
 */
public class MapCache {

    private static final Map<String, List<DictionaryData>> DICTIONARY_CACHE = new HashMap<>(128);
    private static final String ENUM_PACKAGE = "com.lesofn.appboot.common.enums.common";

    private MapCache() {
    }

    static {
        initDictionaryCache();
    }

    @SuppressWarnings("rawtypes")
    private static void initDictionaryCache() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);

            // 将包名转换为路径模式
            String packageSearchPath = "classpath*:" + ENUM_PACKAGE.replace('.', '/') + "/**/*.class";
            Resource[] resources = resolver.getResources(packageSearchPath);

            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    String className = metadataReader.getClassMetadata().getClassName();

                    // 检查是否是枚举类并且实现了DictionaryEnum接口
                    if (metadataReader.getClassMetadata().isInterface()) {
                        continue;
                    }

                    Class<?> clazz = ClassUtils.forName(className, MapCache.class.getClassLoader());
                    // 检查是否是枚举类
                    if (!clazz.isEnum()) {
                        continue;
                    }
                    if (DictionaryEnum.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(Dictionary.class)) {
                        DictionaryEnum[] enumConstants = (DictionaryEnum[]) clazz.getEnumConstants();
                        if (enumConstants != null && enumConstants.length > 0) {
                            loadInCache(enumConstants);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize dictionary cache", e);
        }
    }


    public static Map<String, List<DictionaryData>> dictionaryCache() {
        return DICTIONARY_CACHE;
    }

    private static void loadInCache(DictionaryEnum[] dictionaryEnums) {
        DICTIONARY_CACHE.put(getDictionaryName(dictionaryEnums[0].getClass()), arrayToList(dictionaryEnums));
    }


    private static String getDictionaryName(Class<?> clazz) {
        Objects.requireNonNull(clazz);
        Dictionary annotation = clazz.getAnnotation(Dictionary.class);

        Objects.requireNonNull(annotation);
        return annotation.name();
    }

    @SuppressWarnings("rawtypes")
    private static List<DictionaryData> arrayToList(DictionaryEnum[] dictionaryEnums) {
        if (ArrayUtils.isEmpty(dictionaryEnums)) {
            return Collections.emptyList();
        }
        return Arrays.stream(dictionaryEnums).map(DictionaryData::new).collect(Collectors.toList());
    }


}
