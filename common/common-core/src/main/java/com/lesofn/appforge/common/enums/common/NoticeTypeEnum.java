package com.lesofn.appforge.common.enums.common;

import com.lesofn.appforge.common.enums.DictionaryEnum;
import com.lesofn.appforge.common.enums.dictionary.CssTag;
import com.lesofn.appforge.common.enums.dictionary.Dictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 对应sys_notice的 notice_type字段
 * 名称一般由对应的表名.字段构成
 * 全局的话使用common作为表名
 * @author sofn
 */
@Getter
@AllArgsConstructor
@Dictionary(name = "sysNotice.noticeType")
public enum NoticeTypeEnum implements DictionaryEnum {

    /**
     * 通知类型
     */
    NOTIFICATION(1, "通知", CssTag.WARNING),
    ANNOUNCEMENT(2, "公告", CssTag.SUCCESS);

    private final int value;
    private final String description;
    private final String cssTag;

}
