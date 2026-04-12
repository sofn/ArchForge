package com.lesofn.appforge.common.enums.common;

import com.lesofn.appforge.common.enums.DictionaryEnum;
import com.lesofn.appforge.common.enums.dictionary.CssTag;
import com.lesofn.appforge.common.enums.dictionary.Dictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态
 * @author sofn
 */
@Getter
@AllArgsConstructor
@Dictionary(name = "sysLoginLog.status")
public enum LoginStatusEnum implements DictionaryEnum {
    /**
     * status of user
     */
    LOGIN_SUCCESS(1, "登录成功", CssTag.SUCCESS),
    LOGOUT(2, "退出成功", CssTag.INFO),
    REGISTER(3, "注册", CssTag.PRIMARY),
    LOGIN_FAIL(0, "登录失败", CssTag.DANGER);

    private final int value;
    private final String description;
    private final String cssTag;
}
