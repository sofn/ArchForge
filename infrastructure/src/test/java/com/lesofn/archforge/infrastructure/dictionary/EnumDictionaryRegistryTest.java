package com.lesofn.archforge.infrastructure.dictionary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.infrastructure.config.ArchForgeConfig;
import java.util.List;
import org.junit.jupiter.api.Test;

public class EnumDictionaryRegistryTest {

    @Test
    void shouldLoadYesOrNoEnum() {
        ArchForgeConfig config = new ArchForgeConfig();
        config.getDictionary().setEnumBasePackages(List.of("com.lesofn.archforge.common.enums.common"));
        EnumDictionaryRegistry registry = new EnumDictionaryRegistry(config);
        registry.init();

        assertTrue(registry.findByCode("common.yesOrNo").isPresent());
        EnumDictionary dict = registry.findByCode("common.yesOrNo").get();
        assertEquals("是否", dict.getDictName());
        assertEquals(2, dict.getItems().size());

        EnumDictionaryItem yes = dict.getItems().get(0);
        assertEquals("1", yes.getCode());
        assertEquals("是", yes.getLabel());
    }

    @Test
    void shouldNotLoadEnumWithoutDictionaryAnnotation() {
        ArchForgeConfig config = new ArchForgeConfig();
        config.getDictionary().setEnumBasePackages(List.of("com.lesofn.archforge.server.admin.service.cache"));
        EnumDictionaryRegistry registry = new EnumDictionaryRegistry(config);
        registry.init();

        assertFalse(registry.findByCode("cacheKey").isPresent());
    }
}
