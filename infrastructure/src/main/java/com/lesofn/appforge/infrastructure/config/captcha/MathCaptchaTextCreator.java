package com.lesofn.appforge.infrastructure.config.captcha;

import com.google.code.kaptcha.text.impl.DefaultTextCreator;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 数学验证码文本生成器
 *
 * @author sofn
 */
public class MathCaptchaTextCreator extends DefaultTextCreator {

    @Override
    public String getText() {
        int result;
        StringBuilder suChinese = new StringBuilder();
        // 随机选择运算符：0=乘法，1=除法或加法，2=减法
        int randomOperator = ThreadLocalRandom.current().nextInt(3);

        if (randomOperator == 0) {
            // 乘法运算：两个1-9之间的数字相乘
            int x = ThreadLocalRandom.current().nextInt(1, 10);
            int y = ThreadLocalRandom.current().nextInt(1, 10);
            result = x * y;
            suChinese.append(x).append("×").append(y);
        } else if (randomOperator == 1) {
            // 除法或加法运算
            int x = ThreadLocalRandom.current().nextInt(1, 10);
            int y = ThreadLocalRandom.current().nextInt(1, 20);
            // 如果x不为0且y能被x整除，则进行除法运算，确保结果为整数
            if (x != 0 && y % x == 0) {
                result = y / x;
                suChinese.append(y).append("÷").append(x);
            } else {
                // 否则进行加法运算
                result = x + y;
                suChinese.append(x).append("+").append(y);
            }
        } else {
            // 减法运算：确保结果为非负数
            int x = ThreadLocalRandom.current().nextInt(1, 20);
            int y = ThreadLocalRandom.current().nextInt(1, 20);
            result = Math.max(x, y) - Math.min(x, y);
            suChinese.append(Math.max(x, y)).append("-").append(Math.min(x, y));
        }
        suChinese.append("=?@").append(result);
        return suChinese.toString();
    }
}