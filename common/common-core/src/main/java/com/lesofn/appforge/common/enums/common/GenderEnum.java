package com.lesofn.appforge.common.enums.common;

import com.lesofn.appforge.common.enums.DictionaryEnum;
import com.lesofn.appforge.common.enums.dictionary.CssTag;
import com.lesofn.appforge.common.enums.dictionary.Dictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 对应sys_user的sex字段
 *
 * @author sofn
 */
@Getter
@AllArgsConstructor
@Dictionary(name = "sysUser.sex")
public enum GenderEnum implements DictionaryEnum {

    /** 用户性别 */
    MALE(0, "男", CssTag.PRIMARY),
    FEMALE(1, "女", CssTag.PRIMARY),
    UNKNOWN(2, "未知", CssTag.PRIMARY);

    private final int value;
    private final String description;
    private final String cssTag;
}
