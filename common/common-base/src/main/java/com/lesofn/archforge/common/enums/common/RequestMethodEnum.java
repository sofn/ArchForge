package com.lesofn.archforge.common.enums.common;

import com.lesofn.archforge.common.enums.BasicEnum;
import com.lesofn.archforge.common.enums.dictionary.Dictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Http Method
 *
 * @author sofn
 */
@Getter
@AllArgsConstructor
@Dictionary(name = "common.requestMethod", label = "请求方式")
public enum RequestMethodEnum implements BasicEnum {

    /** 菜单类型 */
    GET(1, "GET"),
    POST(2, "POST"),
    PUT(3, "PUT"),
    DELETE(4, "DELETE"),
    UNKNOWN(-1, "UNKNOWN");

    private final int value;
    private final String description;
}
