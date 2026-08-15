package com.lesofn.archforge.infrastructure.dictionary;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EnumDictionary {

    private Long dictTypeId;
    private String dictCode;
    private String dictName;
    private String description;
    private Integer status;
    private Integer sort;
    private List<EnumDictionaryItem> items;
}
