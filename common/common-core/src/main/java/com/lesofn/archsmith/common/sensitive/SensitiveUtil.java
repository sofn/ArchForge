package com.lesofn.archsmith.common.sensitive;

import org.apache.commons.lang3.StringUtils;

/**
 * 数据脱敏工具类
 *
 * @author sofn
 */
public final class SensitiveUtil {

    private static final String MASK_CHAR = "*";

    private SensitiveUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 根据类型进行脱敏
     *
     * @param type 脱敏类型
     * @param value 原始值
     * @return 脱敏后的值
     */
    public static String mask(SensitiveType type, String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        return switch (type) {
            case CHINESE_NAME -> maskChineseName(value);
            case PHONE -> maskPhone(value);
            case EMAIL -> maskEmail(value);
            case ID_CARD -> maskIdCard(value);
            case BANK_CARD -> maskBankCard(value);
            case ADDRESS -> maskAddress(value);
            case PASSWORD -> maskPassword(value);
            case IP_ADDRESS -> maskIpAddress(value);
        };
    }

    /** 中文名脱敏：保留第一个字，其余用*代替 张三 → 张* 张三丰 → 张** */
    public static String maskChineseName(String name) {
        if (StringUtils.isBlank(name)) {
            return name;
        }
        if (name.length() == 1) {
            return MASK_CHAR;
        }
        return name.charAt(0) + MASK_CHAR.repeat(name.length() - 1);
    }

    /** 手机号脱敏：保留前3位和后4位 13812345678 → 138****5678 */
    public static String maskPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return phone;
        }
        if (phone.length() < 7) {
            return maskCenter(phone, 1, 1);
        }
        return maskCenter(phone, 3, 4);
    }

    /** 邮箱脱敏：保留第一个字符和@后面的域名 zhangsan@example.com → z***@example.com */
    public static String maskEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }
        return email.charAt(0) + MASK_CHAR.repeat(atIndex - 1) + email.substring(atIndex);
    }

    /** 身份证号脱敏：保留前3位和后4位 110101199001011234 → 110***********1234 */
    public static String maskIdCard(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return idCard;
        }
        if (idCard.length() < 7) {
            return maskCenter(idCard, 1, 1);
        }
        return maskCenter(idCard, 3, 4);
    }

    /** 银行卡号脱敏：保留前4位和后4位 6222021234567890123 → 6222***********0123 */
    public static String maskBankCard(String bankCard) {
        if (StringUtils.isBlank(bankCard)) {
            return bankCard;
        }
        if (bankCard.length() < 8) {
            return maskCenter(bankCard, 2, 2);
        }
        return maskCenter(bankCard, 4, 4);
    }

    /** 地址脱敏：保留前6个字符，后面用*代替 北京市海淀区中关村大街1号 → 北京市海淀区****** */
    public static String maskAddress(String address) {
        if (StringUtils.isBlank(address)) {
            return address;
        }
        int keepLength = Math.min(6, address.length());
        if (address.length() <= keepLength) {
            return address;
        }
        return address.substring(0, keepLength) + MASK_CHAR.repeat(address.length() - keepLength);
    }

    /** 密码脱敏：全部用*代替 password123 → ****** */
    public static String maskPassword(String password) {
        if (StringUtils.isBlank(password)) {
            return password;
        }
        return MASK_CHAR.repeat(6);
    }

    /** IP地址脱敏：保留第一段，其余用*代替 192.168.1.100 → 192.*.*.* */
    public static String maskIpAddress(String ip) {
        if (StringUtils.isBlank(ip)) {
            return ip;
        }
        int firstDot = ip.indexOf('.');
        if (firstDot < 0) {
            return ip;
        }
        return ip.substring(0, firstDot) + ".*.*.*";
    }

    /** 保留前后指定位数，中间用*代替 */
    private static String maskCenter(String value, int prefixLength, int suffixLength) {
        int length = value.length();
        if (length <= prefixLength + suffixLength) {
            return value;
        }
        int maskLength = length - prefixLength - suffixLength;
        return value.substring(0, prefixLength)
                + MASK_CHAR.repeat(maskLength)
                + value.substring(length - suffixLength);
    }
}
