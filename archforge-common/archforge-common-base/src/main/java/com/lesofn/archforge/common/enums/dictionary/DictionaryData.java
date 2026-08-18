package com.lesofn.archforge.common.enums.dictionary;

import com.lesofn.archforge.common.enums.BasicEnum;
import com.lesofn.archforge.common.enums.DictionaryEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 字典模型类
 */
@Data
@AllArgsConstructor
public class DictionaryData {

    private String label;
    private Integer value;
    private String cssTag;

    public DictionaryData(BasicEnum enumType) {
        if (enumType != null) {
            this.label = enumType.getDescription();
            this.value = enumType.getValue();
            this.cssTag = enumType instanceof DictionaryEnum d ? d.getCssTag() : null;
        }
    }
}
