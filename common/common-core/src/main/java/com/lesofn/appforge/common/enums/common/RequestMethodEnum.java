package com.lesofn.appforge.common.enums.common;

import com.lesofn.appforge.common.enums.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Http Method
 * @author sofn
 */
@Getter
@AllArgsConstructor
public enum RequestMethodEnum implements BasicEnum {

    /**
     * 菜单类型
     */
    GET(1, "GET"),
    POST(2, "POST"),
    PUT(3, "PUT"),
    DELETE(4, "DELETE"),
    UNKNOWN(-1, "UNKNOWN");

    private final int value;
    private final String description;

}
