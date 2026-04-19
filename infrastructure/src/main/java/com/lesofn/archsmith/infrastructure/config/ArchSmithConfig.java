package com.lesofn.archsmith.infrastructure.config;

import java.util.List;
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

    /** Flyway数据库迁移配置 */
    private Flyway flyway = new Flyway();

    /** 服务器监控配置 */
    private Monitor monitor = new Monitor();

    /** 登录安全配置 */
    private Login login = new Login();

    /** CORS 跨域配置 */
    private Cors cors = new Cors();

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

        /** 是否启用嵌入式 S3 (RustFS) */
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

        /** 最大文件大小 (字节，默认 10MB) */
        private long maxFileSize = 10L * 1024 * 1024;

        /** 允许的文件扩展名白名单（不带点号） */
        private List<String> allowedExtensions =
                List.of(
                        "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "pdf", "doc", "docx",
                        "xls", "xlsx", "ppt", "pptx", "txt", "csv", "zip", "rar", "7z", "mp3",
                        "mp4", "avi", "mov", "json", "xml");

        /** 禁用的 MIME 类型黑名单 */
        private List<String> blockedMimeTypes =
                List.of(
                        "application/x-msdownload",
                        "application/x-sh",
                        "application/x-executable",
                        "application/x-ms-installer",
                        "application/x-bat",
                        "application/javascript");

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

    @Setter
    @Getter
    public static class Flyway {
        /** 是否启用Flyway数据库迁移 */
        private boolean enabled = false;
    }

    @Setter
    @Getter
    public static class Monitor {
        /** 是否启用服务器监控 */
        private boolean enabled = true;
    }

    @Setter
    @Getter
    public static class Login {
        /** 登录失败最大尝试次数（达到后锁定账户） */
        private int maxAttempts = 5;

        /** 账户锁定时长（秒，默认 10 分钟） */
        private int lockoutSeconds = 600;
    }

    @Setter
    @Getter
    public static class Cors {
        /** 允许的来源列表（生产环境应设置具体域名，dev 可用 "*"） */
        private List<String> allowedOrigins = List.of("*");

        /** 允许的 HTTP 方法 */
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");

        /** 允许的请求头 */
        private List<String> allowedHeaders = List.of("*");

        /** 暴露给浏览器的响应头 */
        private List<String> exposedHeaders = List.of("Authorization");

        /** 是否允许携带凭证 */
        private boolean allowCredentials = true;

        /** 预检请求缓存时长（秒） */
        private long maxAge = 3600L;
    }
}
