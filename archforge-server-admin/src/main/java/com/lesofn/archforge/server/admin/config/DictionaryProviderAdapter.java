package com.lesofn.archforge.server.admin.config;

import com.lesofn.archforge.meta.table.api.domain.OptionItem;
import com.lesofn.archforge.meta.table.api.service.DictionaryProvider;
import com.lesofn.archforge.user.api.domain.dict.SysDictItem;
import com.lesofn.archforge.user.api.service.dict.SysDictService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DictionaryProviderAdapter implements DictionaryProvider {

    private final SysDictService dictService;

    @Override
    public List<OptionItem> findItems(String dictCode) {
        return dictService.findItemsByTypeCode(dictCode).stream()
                .filter(i -> !Boolean.TRUE.equals(i.getDeleted()))
                .map(this::toOptionItem)
                .toList();
    }

    private OptionItem toOptionItem(SysDictItem item) {
        OptionItem option = new OptionItem();
        option.setLabel(item.getItemLabel());
        option.setValue(item.getItemCode());
        return option;
    }
}
