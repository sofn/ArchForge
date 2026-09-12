package com.lesofn.archforge.common.dictionary;

import lombok.Value;
import org.jspecify.annotations.Nullable;

@Value
public class EnumDictionaryItem {

    Long dictTypeId;
    Long dictItemId;
    String code;
    String label;
    Integer sort;
    Integer status;
    @Nullable String cssTag;
}
