package com.lesofn.archforge.server.admin.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AdminSaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> StpAdminUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        "/auth/getConfig",
                        "/auth/captchaImage",
                        "/auth/refresh-token",
                        "/auth/",
                        "/idempotent/token",
                        "/actuator/health",
                        "/actuator/prometheus",
                        "/actuator/info",
                        "/error");
    }
}
