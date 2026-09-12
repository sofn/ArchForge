package com.lesofn.archforge.common.enums;

/**
 * 带值与描述的基础枚举接口。
 *
 * @author sofn 普通的枚举 接口
 */
public interface BasicEnum {

    /**
     * 获取枚举的值
     *
     * @return 枚举值
     */
    int getValue();

    /**
     * 获取枚举的描述
     *
     * @return 描述
     */
    String getDescription();
}
