package com.lesofn.archsmith.common.enums.common;

import com.lesofn.archsmith.common.enums.DictionaryEnum;
import com.lesofn.archsmith.common.enums.dictionary.CssTag;
import com.lesofn.archsmith.common.enums.dictionary.Dictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 除非表有特殊指明的话，一般用这个枚举代表 status字段
 *
 * @author sofn
 */
@Getter
@AllArgsConstructor
@Dictionary(name = "common.status")
public enum StatusEnum implements DictionaryEnum {
    /** 开关状态 */
    ENABLE(1, "正常", CssTag.PRIMARY),
    DISABLE(0, "停用", CssTag.DANGER);

    private final int value;
    private final String description;
    private final String cssTag;
}
