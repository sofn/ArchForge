package com.lesofn.archforge.server.admin.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.interceptor.SaInterceptor;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import jakarta.annotation.PostConstruct;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AdminSaTokenConfig implements WebMvcConfigurer {

    @PostConstruct
    public void registerStpLogic() {
        SaManager.putStpLogic(StpAdminUtil.STP_LOGIC);
    }

    /** 免登录路径。只放真实存在、确需匿名的端点——预留一个不存在的路径，等于给将来新增的端点默认免鉴权。 */
    public static final List<String> PUBLIC_PATHS = List.of(
            "/admin/auth/login",
            "/admin/auth/getConfig",
            "/admin/auth/captchaImage",
            "/admin/auth/refresh-token",
            "/admin/idempotent/token",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/actuator/health",
            "/actuator/health/**",
            "/livez",
            "/readyz",
            "/actuator/prometheus",
            "/actuator/info",
            "/error");

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> StpAdminUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(PUBLIC_PATHS);
    }
}
