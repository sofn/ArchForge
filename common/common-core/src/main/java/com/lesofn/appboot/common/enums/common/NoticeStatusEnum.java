package com.lesofn.appboot.common.enums.common;

import com.lesofn.appboot.common.enums.DictionaryEnum;
import com.lesofn.appboot.common.enums.dictionary.CssTag;
import com.lesofn.appboot.common.enums.dictionary.Dictionary;

/**
 * 对应sys_notice的 status字段
 * @author sofn
 */
@Dictionary(name = "sysNotice.status")
public enum NoticeStatusEnum implements DictionaryEnum<Integer> {

    /**
     * 通知状态
     */
    OPEN(1, "正常", CssTag.PRIMARY),
    CLOSE(0, "关闭", CssTag.DANGER);

    private final int value;
    private final String description;
    private final String cssTag;

    NoticeStatusEnum(int value, String description, String cssTag) {
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
