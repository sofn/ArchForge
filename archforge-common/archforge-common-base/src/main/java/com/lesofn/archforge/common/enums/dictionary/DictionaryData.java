package com.lesofn.archforge.common.enums.dictionary;

import com.lesofn.archforge.common.enums.BasicEnum;
import com.lesofn.archforge.common.enums.DictionaryEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;

/**
 * 字典模型类
 */
@Data
@AllArgsConstructor
@SuppressWarnings("NullAway.Init") // partial-init ctor tolerates a null enum
public class DictionaryData {

    private String label;
    private Integer value;
    private @Nullable String cssTag;

    public DictionaryData(@Nullable BasicEnum enumType) {
        if (enumType != null) {
            this.label = enumType.getDescription();
            this.value = enumType.getValue();
            this.cssTag = enumType instanceof DictionaryEnum d ? d.getCssTag() : null;
        }
    }
}
