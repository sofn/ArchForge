package com.lesofn.archsmith.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 项目配置
 *
 * @author sofn
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "arch-smith")
public class ArchSmithConfig {

    /** 项目名称 */
    private String name;

    /** 版本 */
    private String version;

    /** 版权年份 */
    private String copyrightYear;

    /** 验证码类型 */
    private CaptchaType captchaType;

    /** RSA私钥，用户前后端交互加解密隐私信息 */
    private String rsaPrivateKey;

    /** 上传路径 */
    private String fileBaseDir;

    /** Token配置 */
    private Token token = new Token();

    /** JWT配置 */
    private Jwt jwt = new Jwt();

    /** 验证码配置 */
    private Captcha captcha = new Captcha();

    /** 注册功能配置 */
    private Register register = new Register();

    /** 嵌入式配置 */
    private Embedded embedded = new Embedded();

    /** 数据脱敏配置 */
    private Sensitive sensitive = new Sensitive();

    /** 文件存储配置 */
    private FileStorage fileStorage = new FileStorage();

    @Setter
    @Getter
    public static class Token {
        /** 令牌自定义标识 */
        private String header = "Authorization";

        /** 令牌密钥 */
        private String secret;

        /** 令牌有效期（默认30分钟） */
        private int expireTime = 30;

        /** 自动刷新时间（分钟） */
        private int autoRefreshTime = 20;
    }

    @Setter
    @Getter
    public static class Jwt {
        /** JWT密钥 */
        private String secret;

        /** JWT有效期（秒） */
        private long expireSeconds = 604800;
    }

    @Setter
    @Getter
    public static class Captcha {
        /** 是否开启验证码 */
        private boolean enabled = true;
    }

    @Setter
    @Getter
    public static class Register {
        /** 是否开启注册功能 */
        private boolean enabled = false;
    }

    @Setter
    @Getter
    public static class Embedded {
        /** 是否启用嵌入式 Redis */
        private boolean redis = false;

        /** 是否启用嵌入式 PostgreSQL */
        private boolean postgresql = false;

        /** 是否启用数据库初始化 */
        private boolean dbInit = false;

        /** 是否启用嵌入式 S3 (MinIO) */
        private boolean s3 = false;
    }

    @Setter
    @Getter
    public static class Sensitive {
        /** 是否启用数据脱敏 */
        private boolean enabled = true;
    }

    @Setter
    @Getter
    public static class FileStorage {
        /** 存储类型: local 或 s3 */
        private String type = "local";

        /** 本地存储基础目录 */
        private String localDir = "uploads";

        /** S3 配置 */
        private S3Config s3 = new S3Config();
    }

    @Setter
    @Getter
    public static class S3Config {
        /** S3 endpoint */
        private String endpoint = "http://localhost:9000";

        /** S3 access key */
        private String accessKey = "minioadmin";

        /** S3 secret key */
        private String secretKey = "minioadmin";

        /** S3 bucket name */
        private String bucket = "archsmith";

        /** S3 region */
        private String region = "us-east-1";
    }
}
