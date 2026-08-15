package com.lesofn.archforge.user.api.enums;

import com.lesofn.archforge.common.enums.BasicEnum;
import com.lesofn.archforge.common.enums.dictionary.Dictionary;
import lombok.Getter;

/**
 * @author sofn 对应 sys_menu表的menu_type字段
 */
@Getter
@Dictionary(name = "sysMenu.menuType", label = "菜单类型")
public enum MenuTypeEnum implements BasicEnum {

    /** 菜单类型 */
    MENU(1, "页面"),
    CATALOG(2, "目录"),
    IFRAME(3, "内嵌Iframe"),
    OUTSIDE_LINK_REDIRECT(4, "外链跳转");

    private final int value;
    private final String description;

    MenuTypeEnum(int value, String description) {
        this.value = value;
        this.description = description;
    }
}
