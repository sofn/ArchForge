package com.lesofn.archforge.server.admin.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.interceptor.SaInterceptor;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AdminSaTokenConfig implements WebMvcConfigurer {

    @PostConstruct
    public void registerStpLogic() {
        SaManager.putStpLogic(StpAdminUtil.STP_LOGIC);
    }

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
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/actuator/health",
                        "/actuator/prometheus",
                        "/actuator/info",
                        "/error");
    }
}
