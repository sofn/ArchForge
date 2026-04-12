package com.lesofn.appforge.common.enums.common;

import com.lesofn.appforge.common.enums.BasicEnum;
import com.lesofn.appforge.common.enums.dictionary.Dictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作者类型
 * @author sofn
 */
@Getter
@AllArgsConstructor
@Dictionary(name = "sysOperationLog.operatorType")
public enum OperatorTypeEnum implements BasicEnum {

    /**
     * 菜单类型
     */
    OTHER(1, "其他"),
    WEB(2, "Web用户"),
    MOBILE(3, "手机端用户");

    private final int value;
    private final String description;

}
