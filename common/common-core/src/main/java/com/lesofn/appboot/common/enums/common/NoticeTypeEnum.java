package com.lesofn.appboot.common.enums.common;

import com.lesofn.appboot.common.enums.DictionaryEnum;
import com.lesofn.appboot.common.enums.dictionary.CssTag;
import com.lesofn.appboot.common.enums.dictionary.Dictionary;

/**
 * 对应sys_notice的 notice_type字段
 * 名称一般由对应的表名.字段构成
 * 全局的话使用common作为表名
 * @author sofn
 */
@Dictionary(name = "sysNotice.noticeType")
public enum NoticeTypeEnum implements DictionaryEnum<Integer> {

    /**
     * 通知类型
     */
    NOTIFICATION(1, "通知", CssTag.WARNING),
    ANNOUNCEMENT(2, "公告", CssTag.SUCCESS);

    private final int value;
    private final String description;
    private final String cssTag;

    NoticeTypeEnum(int value, String description, String cssTag) {
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
