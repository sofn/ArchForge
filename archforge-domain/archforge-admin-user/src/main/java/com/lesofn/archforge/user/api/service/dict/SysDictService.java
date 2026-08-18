package com.lesofn.archforge.user.api.service.dict;

import com.lesofn.archforge.user.api.domain.dict.SysDictItem;
import com.lesofn.archforge.user.api.domain.dict.SysDictType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SysDictService {

    Optional<SysDictType> findTypeById(Long dictTypeId);

    Optional<SysDictType> findTypeByCode(String dictCode);

    Page<SysDictType> findTypePage(String keyword, Pageable pageable);

    List<SysDictItem> findItemsByTypeCode(String dictCode);

    List<SysDictItem> findItemsByTypeId(Long dictTypeId);

    Optional<SysDictItem> findItemById(Long dictItemId);

    SysDictType saveType(SysDictType type);

    SysDictType saveTypeWithItems(SysDictType type, List<SysDictItem> items);

    SysDictItem saveItem(Long dictTypeId, SysDictItem item);

    void deleteType(Long dictTypeId);

    void deleteItem(Long dictItemId);
}
