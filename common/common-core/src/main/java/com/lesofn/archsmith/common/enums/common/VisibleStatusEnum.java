package com.lesofn.archsmith.common.enums.common;

import com.lesofn.archsmith.common.enums.DictionaryEnum;
import com.lesofn.archsmith.common.enums.dictionary.CssTag;
import com.lesofn.archsmith.common.enums.dictionary.Dictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 对应sys_menu表的is_visible字段
 *
 * @author sofn
 */
@Getter
@AllArgsConstructor
@Dictionary(name = "sysMenu.isVisible")
public enum VisibleStatusEnum implements DictionaryEnum {

    /** 显示与否 */
    SHOW(1, "显示", CssTag.PRIMARY),
    HIDE(0, "隐藏", CssTag.DANGER);

    private final int value;
    private final String description;
    private final String cssTag;
}
