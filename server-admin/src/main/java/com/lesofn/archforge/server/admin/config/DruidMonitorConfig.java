package com.lesofn.archforge.server.admin.config;

import com.alibaba.druid.support.jakarta.StatViewServlet;
import com.alibaba.druid.support.jakarta.WebStatFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Druid 监控配置。仅在非 prod 环境启用，prod 环境不暴露 /druid/*。
 *
 * @author sofn
 */
@Configuration
@Profile("!prod")
public class DruidMonitorConfig {

    /** StatViewServlet 访问路径 */
    private static final String DRUID_URL_PATTERN = "/druid/*";

    /** WebStatFilter 排除的静态资源与 Druid 自身路径 */
    private static final String EXCLUSIONS = "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*";

    @Value("${arch-forge.druid.login-username:admin}")
    private String loginUsername;

    @Value("${arch-forge.druid.login-password:admin}")
    private String loginPassword;

    /**
     * 注册 Druid StatViewServlet，提供 /druid/login.html 等监控页面。
     * 非 prod 环境启用，默认登录账号可通过 arch-forge.druid.* 配置覆盖。
     */
    @Bean
    public ServletRegistrationBean<StatViewServlet> druidStatViewServlet() {
        ServletRegistrationBean<StatViewServlet> registration = new ServletRegistrationBean<>(new StatViewServlet(), DRUID_URL_PATTERN);
        registration.addInitParameter("resetEnable", "false");
        registration.addInitParameter("loginUsername", loginUsername);
        registration.addInitParameter("loginPassword", loginPassword);
        return registration;
    }

    /**
     * 注册 Druid WebStatFilter，用于采集 Web 请求统计。
     */
    @Bean
    public FilterRegistrationBean<WebStatFilter> druidWebStatFilter() {
        FilterRegistrationBean<WebStatFilter> registration = new FilterRegistrationBean<>(new WebStatFilter());
        registration.addUrlPatterns("/*");
        registration.addInitParameter("exclusions", EXCLUSIONS);
        return registration;
    }
}
