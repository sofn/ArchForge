package com.lesofn.archforge.user.internal.service.dict;

import com.lesofn.archforge.infrastructure.dictionary.EnumDictionary;
import com.lesofn.archforge.infrastructure.dictionary.EnumDictionaryItem;
import com.lesofn.archforge.infrastructure.dictionary.EnumDictionaryRegistry;
import com.lesofn.archforge.user.api.dao.dict.SysDictItemRepository;
import com.lesofn.archforge.user.api.dao.dict.SysDictTypeRepository;
import com.lesofn.archforge.user.api.domain.dict.SysDictItem;
import com.lesofn.archforge.user.api.domain.dict.SysDictType;
import com.lesofn.archforge.user.api.errors.AdminUserErrorCode;
import com.lesofn.archforge.user.api.errors.AdminUserException;
import com.lesofn.archforge.user.api.service.dict.SysDictService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SysDictServiceImpl implements SysDictService {

    private final SysDictTypeRepository typeRepository;
    private final SysDictItemRepository itemRepository;
    private final EnumDictionaryRegistry enumDictionaryRegistry;

    @Override
    public Optional<SysDictType> findTypeById(Long dictTypeId) {
        return typeRepository.findById(dictTypeId);
    }

    @Override
    public Optional<SysDictType> findTypeByCode(String dictCode) {
        return typeRepository.findByDictCode(dictCode)
                .or(() -> enumDictionaryRegistry.findByCode(dictCode).map(this::toSysDictType));
    }

    @Override
    public Page<SysDictType> findTypePage(String keyword, Pageable pageable) {
        List<SysDictType> dbTypes = queryDbTypes(keyword, pageable);
        Set<String> dbCodes = dbTypes.stream()
                .map(SysDictType::getDictCode)
                .collect(Collectors.toSet());
        List<SysDictType> enumTypes = enumDictionaryRegistry.findAll().stream()
                .filter(d -> !dbCodes.contains(d.getDictCode()))
                .filter(d -> matchesKeyword(d, keyword))
                .map(this::toSysDictType)
                .toList();
        List<SysDictType> combined = new ArrayList<>(dbTypes.size() + enumTypes.size());
        combined.addAll(dbTypes);
        combined.addAll(enumTypes);
        combined.sort(Comparator.comparingInt(t -> t.getSort() == null ? 0 : t.getSort()));
        return toPage(combined, pageable);
    }

    private List<SysDictType> queryDbTypes(String keyword, Pageable pageable) {
        Iterable<SysDictType> iterable;
        if (!StringUtils.hasText(keyword)) {
            iterable = typeRepository.findAll(pageable.getSort());
        } else {
            SysDictType probe = new SysDictType();
            probe.setDictCode(null);
            probe.setDictName(keyword);
            ExampleMatcher matcher = ExampleMatcher.matching()
                    .withMatcher("dictName", ExampleMatcher.GenericPropertyMatchers.contains())
                    .withIgnorePaths("status", "sort", "deleted")
                    .withIgnoreNullValues();
            iterable = typeRepository.findAll(Example.of(probe, matcher), pageable.getSort());
        }
        List<SysDictType> result = new ArrayList<>();
        for (SysDictType type : iterable) {
            result.add(type);
        }
        return result;
    }

    private boolean matchesKeyword(EnumDictionary dict, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String lower = keyword.toLowerCase();
        return (dict.getDictCode() != null && dict.getDictCode().toLowerCase().contains(lower)) || (dict
                .getDictName() != null && dict.getDictName().toLowerCase().contains(lower));
    }

    private Page<SysDictType> toPage(List<SysDictType> content, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), content.size());
        List<SysDictType> pageContent = start >= content.size() ? List.of() : content.subList(start, end);
        return new PageImpl<>(pageContent, pageable, content.size());
    }

    @Override
    public List<SysDictItem> findItemsByTypeCode(String dictCode) {
        Optional<SysDictType> dbType = typeRepository.findByDictCode(dictCode);
        if (dbType.isPresent()) {
            return itemRepository.findByDictTypeIdAndStatusAndDeletedFalseOrderBySortAsc(dbType.get().getDictTypeId(), 1);
        }
        return enumDictionaryRegistry.findByCode(dictCode)
                .map(EnumDictionary::getItems)
                .orElse(List.of())
                .stream()
                .map(this::toSysDictItem)
                .toList();
    }

    @Override
    public List<SysDictItem> findItemsByTypeId(Long dictTypeId) {
        List<SysDictItem> dbItems = itemRepository.findByDictTypeIdAndDeletedFalseOrderBySortAsc(dictTypeId);
        if (!dbItems.isEmpty()) {
            return dbItems;
        }
        return enumDictionaryRegistry.findByTypeId(dictTypeId)
                .map(EnumDictionary::getItems)
                .orElse(List.of())
                .stream()
                .map(this::toSysDictItem)
                .toList();
    }

    @Override
    public Optional<SysDictItem> findItemById(Long dictItemId) {
        return itemRepository.findById(dictItemId)
                .or(() -> enumDictionaryRegistry.findItemById(dictItemId).map(this::toSysDictItem));
    }

    @Override
    @Transactional
    public SysDictType saveType(SysDictType type) {
        assertNotEnumDictCode(type.getDictCode());
        if (type.getDictTypeId() != null) {
            assertNotEnumDictTypeId(type.getDictTypeId());
        }
        return typeRepository.save(type);
    }

    @Override
    @Transactional
    public SysDictType saveTypeWithItems(SysDictType type, List<SysDictItem> items) {
        assertNotEnumDictCode(type.getDictCode());
        if (type.getDictTypeId() != null) {
            assertNotEnumDictTypeId(type.getDictTypeId());
        }
        SysDictType saved = typeRepository.save(type);
        if (items == null || items.isEmpty()) {
            return saved;
        }
        List<SysDictItem> toSave = new ArrayList<>();
        for (SysDictItem item : items) {
            item.setDictTypeId(saved.getDictTypeId());
            toSave.add(item);
        }
        itemRepository.saveAll(toSave);
        return saved;
    }

    @Override
    @Transactional
    public SysDictItem saveItem(Long dictTypeId, SysDictItem item) {
        assertNotEnumDictTypeId(dictTypeId);
        if (item.getDictItemId() != null) {
            assertNotEnumDictItemId(item.getDictItemId());
        }
        item.setDictTypeId(dictTypeId);
        return itemRepository.save(item);
    }

    @Override
    @Transactional
    public void deleteType(Long dictTypeId) {
        assertNotEnumDictTypeId(dictTypeId);
        typeRepository.deleteById(dictTypeId);
        List<SysDictItem> items = itemRepository.findByDictTypeIdAndDeletedFalseOrderBySortAsc(dictTypeId);
        itemRepository.deleteAll(items);
    }

    @Override
    @Transactional
    public void deleteItem(Long dictItemId) {
        assertNotEnumDictItemId(dictItemId);
        itemRepository.deleteById(dictItemId);
    }

    private SysDictType toSysDictType(EnumDictionary dict) {
        SysDictType type = new SysDictType();
        type.setDictTypeId(dict.getDictTypeId());
        type.setDictCode(dict.getDictCode());
        type.setDictName(dict.getDictName());
        type.setDescription(dict.getDescription());
        type.setStatus(dict.getStatus());
        type.setSort(dict.getSort());
        return type;
    }

    private SysDictItem toSysDictItem(EnumDictionaryItem item) {
        SysDictItem sys = new SysDictItem();
        sys.setDictItemId(item.getDictItemId());
        sys.setDictTypeId(item.getDictTypeId());
        sys.setItemCode(item.getCode());
        sys.setItemLabel(item.getLabel());
        sys.setSort(item.getSort());
        sys.setStatus(item.getStatus());
        return sys;
    }

    private void assertNotEnumDictCode(String dictCode) {
        if (enumDictionaryRegistry.isEnumDictCode(dictCode)) {
            throw new AdminUserException(AdminUserErrorCode.DICT_READONLY, dictCode);
        }
    }

    private void assertNotEnumDictTypeId(Long dictTypeId) {
        if (enumDictionaryRegistry.isEnumDictTypeId(dictTypeId)) {
            throw new AdminUserException(AdminUserErrorCode.DICT_READONLY, "dictTypeId=" + dictTypeId);
        }
    }

    private void assertNotEnumDictItemId(Long dictItemId) {
        if (enumDictionaryRegistry.isEnumDictItemId(dictItemId)) {
            throw new AdminUserException(AdminUserErrorCode.DICT_READONLY, "dictItemId=" + dictItemId);
        }
    }
}
