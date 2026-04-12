package com.lesofn.appforge.common.enums.common;

import com.lesofn.appforge.common.enums.DictionaryEnum;
import com.lesofn.appforge.common.enums.dictionary.CssTag;
import com.lesofn.appforge.common.enums.dictionary.Dictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 对应sys_user的status字段
 * @author sofn
 */
@Getter
@AllArgsConstructor
@Dictionary(name = "sysUser.status")
public enum UserStatusEnum implements DictionaryEnum {

    /**
     * 用户账户状态
     */
    NORMAL(1, "正常", CssTag.PRIMARY),
    DISABLED(2, "禁用", CssTag.DANGER),
    FROZEN(3, "冻结", CssTag.WARNING);

    private final int value;
    private final String description;

    private final String cssTag;

}
