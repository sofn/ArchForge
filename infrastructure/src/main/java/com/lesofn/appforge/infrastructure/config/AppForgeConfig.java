package com.lesofn.appforge.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 项目配置
 * @author sofn
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "app-forge")
public class AppForgeConfig {

    /**
     * 项目名称
     */
    private String name;

    /**
     * 版本
     */
    private String version;

    /**
     * 版权年份
     */
    private String copyrightYear;

    /**
     * 验证码类型
     */
    private CaptchaType captchaType;

    /**
     * RSA私钥，用户前后端交互加解密隐私信息
     */
    private String rsaPrivateKey;

    /**
     * 上传路径
     */
    private String fileBaseDir;

    /**
     * Token配置
     */
    private Token token = new Token();

    /**
     * JWT配置
     */
    private Jwt jwt = new Jwt();

    /**
     * 验证码配置
     */
    private Captcha captcha = new Captcha();

    /**
     * 注册功能配置
     */
    private Register register = new Register();

    /**
     * 嵌入式配置
     */
    private Embedded embedded = new Embedded();

    @Setter
    @Getter
    public static class Token {
        /**
         * 令牌自定义标识
         */
        private String header = "Authorization";

        /**
         * 令牌密钥
         */
        private String secret;

        /**
         * 令牌有效期（默认30分钟）
         */
        private int expireTime = 30;

        /**
         * 自动刷新时间（分钟）
         */
        private int autoRefreshTime = 20;
    }

    @Setter
    @Getter
    public static class Jwt {
        /**
         * JWT密钥
         */
        private String secret;

        /**
         * JWT有效期（秒）
         */
        private long expireSeconds = 604800;
    }

    @Setter
    @Getter
    public static class Captcha {
        /**
         * 是否开启验证码
         */
        private boolean enabled = true;
    }

    @Setter
    @Getter
    public static class Register {
        /**
         * 是否开启注册功能
         */
        private boolean enabled = false;
    }

    @Setter
    @Getter
    public static class Embedded {
        /**
         * 是否启用嵌入式 Redis
         */
        private boolean redis = false;
        /**
         * 是否启用嵌入式 h2
         */
        private boolean h2Init = false;
    }

}
