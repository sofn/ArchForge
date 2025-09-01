package com.lesofn.appboot.infrastructure.config;

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
@ConfigurationProperties(prefix = "app-boot")
public class AppBootConfig {

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
    private String captchaType;

    /**
     * 上传路径
     */
    private String fileBaseDir;

}
