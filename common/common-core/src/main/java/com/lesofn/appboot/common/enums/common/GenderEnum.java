package com.lesofn.appboot.common.enums.common;

import com.lesofn.appboot.common.enums.DictionaryEnum;
import com.lesofn.appboot.common.enums.dictionary.CssTag;
import com.lesofn.appboot.common.enums.dictionary.Dictionary;

/**
 * 对应sys_user的sex字段
 *
 * @author sofn
 */
@Dictionary(name = "sysUser.sex")
public enum GenderEnum implements DictionaryEnum<Integer> {

    /**
     * 用户性别
     */
    MALE(1, "男", CssTag.PRIMARY),
    FEMALE(2, "女", CssTag.PRIMARY),
    UNKNOWN(0, "未知", CssTag.PRIMARY);

    private final int value;
    private final String description;
    private final String cssTag;

    GenderEnum(int value, String description, String cssTag) {
        this.value = value;
        this.description = description;
        this.cssTag = cssTag;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public String cssTag() {
        return cssTag;
    }

}
