package com.lesofn.appboot.infrastructure.config.captcha;

import com.google.code.kaptcha.text.impl.DefaultTextCreator;

import java.util.Random;

/**
 * 数学验证码文本生成器
 *
 * @author sofn
 */
public class MathCaptchaTextCreator extends DefaultTextCreator {

    private static final String[] NUMBERS = "0123456789".split("");

    private static final Random RANDOM = new Random();

    @Override
    public String getText() {
        int result;
        int x = RANDOM.nextInt(15);
        int y = RANDOM.nextInt(15);
        StringBuilder suChinese = new StringBuilder();
        int randomOperator = RANDOM.nextInt(3);
        if (randomOperator == 0) {
            result = x * y;
            suChinese.append(x).append("×").append(y);
        } else if (randomOperator == 1) {
            if (x != 0 && y % x == 0) {
                result = y / x;
            } else {
                result = x + y;
                suChinese.append(x).append("+").append(y);
            }
            if (result == y / x) {
                suChinese.append(y).append("÷").append(x);
            }
        } else {
            if (x >= y) {
                result = x - y;
                suChinese.append(x).append("-").append(y);
            } else {
                result = y - x;
                suChinese.append(y).append("-").append(x);
            }
        }
        suChinese.append("=?@").append(result);
        return suChinese.toString();
    }
}