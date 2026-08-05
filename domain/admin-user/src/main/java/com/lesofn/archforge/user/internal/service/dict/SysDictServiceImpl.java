package com.lesofn.archforge.user.internal.service.dict;

import com.lesofn.archforge.user.api.dao.dict.SysDictItemRepository;
import com.lesofn.archforge.user.api.dao.dict.SysDictTypeRepository;
import com.lesofn.archforge.user.api.domain.dict.SysDictItem;
import com.lesofn.archforge.user.api.domain.dict.SysDictType;
import com.lesofn.archforge.user.api.service.dict.SysDictService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysDictServiceImpl implements SysDictService {

    private final SysDictTypeRepository typeRepository;
    private final SysDictItemRepository itemRepository;

    @Override
    public Optional<SysDictType> findTypeById(Long dictTypeId) {
        return typeRepository.findById(dictTypeId);
    }

    @Override
    public Optional<SysDictType> findTypeByCode(String dictCode) {
        return typeRepository.findByDictCode(dictCode);
    }

    @Override
    public Page<SysDictType> findTypePage(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return typeRepository.findAll(pageable);
        }
        SysDictType probe = new SysDictType();
        probe.setDictCode(null);
        probe.setDictName(keyword);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("dictName", ExampleMatcher.GenericPropertyMatchers.contains())
                .withIgnorePaths("status", "sort", "deleted")
                .withIgnoreNullValues();
        return typeRepository.findAll(Example.of(probe, matcher), pageable);
    }

    @Override
    public List<SysDictItem> findItemsByTypeCode(String dictCode) {
        return typeRepository.findByDictCode(dictCode)
                .map(t -> itemRepository.findByDictTypeIdAndStatusAndDeletedFalseOrderBySortAsc(t.getDictTypeId(), 1))
                .orElse(List.of());
    }

    @Override
    public List<SysDictItem> findItemsByTypeId(Long dictTypeId) {
        return itemRepository.findByDictTypeIdAndDeletedFalseOrderBySortAsc(dictTypeId);
    }

    @Override
    public Optional<SysDictItem> findItemById(Long dictItemId) {
        return itemRepository.findById(dictItemId);
    }

    @Override
    @Transactional
    public SysDictType saveType(SysDictType type) {
        return typeRepository.save(type);
    }

    @Override
    @Transactional
    public SysDictType saveTypeWithItems(SysDictType type, List<SysDictItem> items) {
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
        item.setDictTypeId(dictTypeId);
        return itemRepository.save(item);
    }

    @Override
    @Transactional
    public void deleteType(Long dictTypeId) {
        typeRepository.deleteById(dictTypeId);
        List<SysDictItem> items = itemRepository.findByDictTypeIdAndDeletedFalseOrderBySortAsc(dictTypeId);
        itemRepository.deleteAll(items);
    }

    @Override
    @Transactional
    public void deleteItem(Long dictItemId) {
        itemRepository.deleteById(dictItemId);
    }
}
