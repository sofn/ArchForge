package com.lesofn.archforge.user.api.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class MenuTypeEnumContractTest {

    @Test
    void matchesBackendCanonicalValues() {
        Map<String, Integer> values = Arrays.stream(MenuTypeEnum.values())
                .collect(Collectors.toMap(Enum::name, MenuTypeEnum::getValue));
        assertEquals(1, values.get("MENU"));
        assertEquals(2, values.get("CATALOG"));
        assertEquals(3, values.get("IFRAME"));
        assertEquals(4, values.get("OUTSIDE_LINK_REDIRECT"));
        assertNull(values.get("BUTTON"));
        assertEquals(4, MenuTypeEnum.values().length);
    }
}
