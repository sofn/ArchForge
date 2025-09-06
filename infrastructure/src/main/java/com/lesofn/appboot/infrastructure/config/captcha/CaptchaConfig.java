package com.lesofn.appboot.infrastructure.config.captcha;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.impl.NoNoise;
import com.google.code.kaptcha.impl.WaterRipple;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 验证码配置类
 *
 * @author sofn
 */
@Configuration
public class CaptchaConfig {

    /**
     * 字符验证码生成器
     */
    @Bean(name = "captchaProducer")
    public Producer captchaProducer() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        
        // 是否有边框
        properties.setProperty("kaptcha.border", "yes");
        // 边框颜色
        properties.setProperty("kaptcha.border.color", "105,179,90");
        // 验证码文本字符颜色
        properties.setProperty("kaptcha.textproducer.font.color", "blue");
        // 验证码图片宽度
        properties.setProperty("kaptcha.image.width", "110");
        // 验证码图片高度
        properties.setProperty("kaptcha.image.height", "40");
        // 验证码文本字符大小
        properties.setProperty("kaptcha.textproducer.font.size", "30");
        // 验证码会话键
        properties.setProperty("kaptcha.session.key", "code");
        // 验证码文本字符长度
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 验证码文本字体样式
        properties.setProperty("kaptcha.textproducer.font.names", "Arial,Courier");
        // 验证码噪点颜色
        properties.setProperty("kaptcha.noise.color", "white");
        // 验证码文本字符内容范围
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");

        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);

        return defaultKaptcha;
    }

    /**
     * 数学验证码生成器
     */
    @Bean(name = "captchaProducerMath")
    public Producer captchaProducerMath() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        
        // 是否有边框
        properties.setProperty("kaptcha.border", "yes");
        // 边框颜色
        properties.setProperty("kaptcha.border.color", "105,179,90");
        // 验证码文本字符颜色
        properties.setProperty("kaptcha.textproducer.font.color", "blue");
        // 验证码图片宽度
        properties.setProperty("kaptcha.image.width", "110");
        // 验证码图片高度
        properties.setProperty("kaptcha.image.height", "40");
        // 验证码文本字符大小
        properties.setProperty("kaptcha.textproducer.font.size", "30");
        // 验证码会话键
        properties.setProperty("kaptcha.session.key", "mathCode");
        // 验证码文本生成器
        properties.setProperty("kaptcha.textproducer.impl", MathCaptchaTextCreator.class.getName());
        // 验证码文本字符间距
        properties.setProperty("kaptcha.textproducer.char.space", "3");
        // 验证码文本字符长度
        properties.setProperty("kaptcha.textproducer.char.length", "6");
        // 验证码文本字体样式
        properties.setProperty("kaptcha.textproducer.font.names", "Arial,Courier");
        // 验证码噪点颜色
        properties.setProperty("kaptcha.noise.color", "white");
        // 干扰实现类
        properties.setProperty("kaptcha.noise.impl", NoNoise.class.getName());
        // 图片样式
        properties.setProperty("kaptcha.obscurificator.impl", WaterRipple.class.getName());

        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);

        return defaultKaptcha;
    }
}