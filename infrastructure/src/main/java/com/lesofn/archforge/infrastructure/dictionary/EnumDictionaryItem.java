package com.lesofn.archforge.infrastructure.dictionary;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EnumDictionaryItem {

    private Long dictTypeId;
    private Long dictItemId;
    private String code;
    private String label;
    private Integer sort;
    private Integer status;
    private String cssTag;
}
