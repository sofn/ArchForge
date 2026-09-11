package com.lesofn.archforge.common.dictionary;

import lombok.Value;

@Value
public class EnumDictionaryItem {

    Long dictTypeId;
    Long dictItemId;
    String code;
    String label;
    Integer sort;
    Integer status;
    String cssTag;
}
