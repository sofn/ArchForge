package com.lesofn.appforge.user.enums;

import com.lesofn.appforge.common.enums.BasicEnum;
import lombok.Getter;

/**
 * @author sofn 对应 sys_menu表的menu_type字段
 */
@Getter
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
