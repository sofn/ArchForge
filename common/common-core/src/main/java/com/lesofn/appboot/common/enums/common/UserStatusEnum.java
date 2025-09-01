package com.lesofn.appboot.common.enums.common;

import com.lesofn.appboot.common.enums.DictionaryEnum;
import com.lesofn.appboot.common.enums.dictionary.CssTag;
import com.lesofn.appboot.common.enums.dictionary.Dictionary;

/**
 * 对应sys_user的status字段
 * @author sofn
 */
@Dictionary(name = "sysUser.status")
public enum UserStatusEnum implements DictionaryEnum<Integer> {

    /**
     * 用户账户状态
     */
    NORMAL(1, "正常", CssTag.PRIMARY),
    DISABLED(2, "禁用", CssTag.DANGER),
    FROZEN(3, "冻结", CssTag.WARNING);

    private final int value;
    private final String description;

    private final String cssTag;

    UserStatusEnum(int value, String description, String cssTag) {
        this.value = value;
        this.description = description;
        this.cssTag = cssTag;
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public String description() {
        return this.description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String cssTag() {
        return null;
    }
}
