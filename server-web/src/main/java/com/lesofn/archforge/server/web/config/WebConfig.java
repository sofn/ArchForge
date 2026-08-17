package com.lesofn.archforge.server.web.config;

import cn.dev33.satoken.SaManager;
import com.lesofn.archforge.infrastructure.auth.stp.StpWebUtil;
import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import com.lesofn.archforge.server.web.auth.MockWebAuthInterceptor;
import com.lesofn.archforge.server.web.interceptor.WebAuthInterceptor;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;
    private final WebAuthInterceptor webAuthInterceptor;
    private final org.springframework.beans.factory.ObjectProvider<MockWebAuthInterceptor> mockWebAuthInterceptor;
    private final ArchForgeProperties archForgeConfig;
    private final Environment environment;

    @PostConstruct
    public void registerStpLogic() {
        SaManager.putStpLogic(StpWebUtil.STP_LOGIC);
    }

    @Bean
    public JacksonJsonHttpMessageConverter jacksonJsonHttpMessageConverter() {
        JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter((JsonMapper) objectMapper);
        converter.setSupportedMediaTypes(
                Arrays.asList(MediaType.APPLICATION_JSON, MediaType.MULTIPART_FORM_DATA));
        return converter;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        boolean has = converters.stream()
                .anyMatch(c -> c instanceof JacksonJsonHttpMessageConverter);
        if (!has) {
            converters.add(0, jacksonJsonHttpMessageConverter());
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        MockWebAuthInterceptor mockInterceptor = mockWebAuthInterceptor.getIfAvailable();
        if (mockInterceptor != null) {
            registry.addInterceptor(mockInterceptor)
                    .addPathPatterns("/web/**")
                    .order(mockInterceptor.getOrder());
        }
        registry.addInterceptor(webAuthInterceptor)
                .addPathPatterns("/web/**")
                .excludePathPatterns(
                        "/web/login",
                        "/web/refresh-token",
                        "/web/categories",
                        "/web/verification-code/send",
                        "/web/register",
                        "/web/forgot-password",
                        "/web/reset-password");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> allowedOrigins = Objects.requireNonNullElse(
                archForgeConfig.getCors().getAllowedOrigins(), List.of());
        if (environment.matchesProfiles("prod") && (allowedOrigins.isEmpty() || allowedOrigins.contains("*"))) {
            throw new IllegalStateException("生产环境 CORS 必须配置具体的 allowedOrigins（arch-forge.cors.allowed-origins）");
        }
        org.springframework.web.servlet.config.annotation.CorsRegistration registration = registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
        if (allowedOrigins.isEmpty() || (allowedOrigins.size() == 1 && "*".equals(allowedOrigins.get(0)))) {
            registration.allowedOriginPatterns("*");
        } else {
            registration.allowedOrigins(allowedOrigins.toArray(String[]::new));
        }
    }
}
