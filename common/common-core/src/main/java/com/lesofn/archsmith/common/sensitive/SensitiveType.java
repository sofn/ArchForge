package com.lesofn.archsmith.common.sensitive;

/**
 * 数据脱敏类型
 *
 * @author sofn
 */
public enum SensitiveType {

    /** 中文名：张** */
    CHINESE_NAME,

    /** 手机号：138****1234 */
    PHONE,

    /** 邮箱：z***@example.com */
    EMAIL,

    /** 身份证号：110***********1234 */
    ID_CARD,

    /** 银行卡号：6222***********1234 */
    BANK_CARD,

    /** 地址：北京市海淀区**** */
    ADDRESS,

    /** 密码：****** */
    PASSWORD,

    /** IP地址：192.*.*.* */
    IP_ADDRESS,
}
