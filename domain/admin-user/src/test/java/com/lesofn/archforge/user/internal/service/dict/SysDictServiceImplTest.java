package com.lesofn.archforge.user.internal.service.dict;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.infrastructure.dictionary.EnumDictionary;
import com.lesofn.archforge.infrastructure.dictionary.EnumDictionaryItem;
import com.lesofn.archforge.infrastructure.dictionary.EnumDictionaryRegistry;
import com.lesofn.archforge.user.api.dao.dict.SysDictItemRepository;
import com.lesofn.archforge.user.api.dao.dict.SysDictTypeRepository;
import com.lesofn.archforge.user.api.domain.dict.SysDictType;
import com.lesofn.archforge.user.api.errors.AdminUserException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SysDictServiceImplTest {

    @Mock
    private SysDictTypeRepository typeRepository;
    @Mock
    private SysDictItemRepository itemRepository;
    @Mock
    private EnumDictionaryRegistry enumDictionaryRegistry;

    @InjectMocks
    private SysDictServiceImpl service;

    @Test
    void shouldFallbackToEnumWhenTypeCodeNotInDb() {
        when(typeRepository.findByDictCode("common.yesOrNo")).thenReturn(Optional.empty());
        EnumDictionary dict = new EnumDictionary(-1L, "common.yesOrNo", "是否", null, 1, 0, List.of(
                new EnumDictionaryItem(-1L, -1L, "1", "是", 0, 1, null),
                new EnumDictionaryItem(-1L, -2L, "0", "否", 1, 1, null)));
        when(enumDictionaryRegistry.findByCode("common.yesOrNo")).thenReturn(Optional.of(dict));

        Optional<SysDictType> result = service.findTypeByCode("common.yesOrNo");

        assertTrue(result.isPresent());
        assertEquals("common.yesOrNo", result.get().getDictCode());
        assertEquals("是否", result.get().getDictName());
    }

    @Test
    void shouldRejectSavingEnumType() {
        when(enumDictionaryRegistry.isEnumDictCode("common.yesOrNo")).thenReturn(true);

        SysDictType type = new SysDictType();
        type.setDictCode("common.yesOrNo");

        assertThrows(AdminUserException.class, () -> service.saveType(type));
    }
}
