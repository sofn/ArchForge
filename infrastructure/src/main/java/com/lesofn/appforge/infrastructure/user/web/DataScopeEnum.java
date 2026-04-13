package com.lesofn.appforge.infrastructure.user.web;

import com.lesofn.appforge.common.enums.BasicEnum;
import lombok.Getter;

/**
 * 对应sys_role表的data_scope字段
 *
 * @author sofn
 */
@Getter
public enum DataScopeEnum implements BasicEnum {

    /** 数据权限范围 */
    ALL(1, "所有数据权限"),
    CUSTOM_DEFINE(2, "自定义数据权限"),
    SINGLE_DEPT(3, "本部门数据权限"),
    DEPT_TREE(4, "本部门以及子孙部门数据权限"),
    ONLY_SELF(5, "仅本人数据权限");

    private final int value;
    private final String description;

    DataScopeEnum(int value, String description) {
        this.value = value;
        this.description = description;
    }
}
