package com.lesofn.archforge.common.dictionary;

import java.util.List;
import org.jspecify.annotations.Nullable;
import lombok.Value;

@Value
public class EnumDictionary {

    Long dictTypeId;
    String dictCode;
    String dictName;
    @Nullable
    String description;
    Integer status;
    Integer sort;
    List<EnumDictionaryItem> items;

    public EnumDictionary(Long dictTypeId, String dictCode, String dictName,
            @Nullable String description, Integer status, Integer sort,
            List<EnumDictionaryItem> items) {
        this.dictTypeId = dictTypeId;
        this.dictCode = dictCode;
        this.dictName = dictName;
        this.description = description;
        this.status = status;
        this.sort = sort;
        this.items = List.copyOf(items);
    }
}
