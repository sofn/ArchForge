package com.lesofn.archforge.infrastructure.dictionary;

import java.util.List;
import lombok.Value;

@Value
public class EnumDictionary {

    Long dictTypeId;
    String dictCode;
    String dictName;
    String description;
    Integer status;
    Integer sort;
    List<EnumDictionaryItem> items;

    public EnumDictionary(Long dictTypeId, String dictCode, String dictName,
            String description, Integer status, Integer sort,
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
