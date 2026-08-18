package com.lesofn.archforge.server.admin.config;

import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@RequiredArgsConstructor
public class AdminCorsConfig {

    private final ArchForgeProperties archForgeConfig;
    private final Environment environment;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        ArchForgeProperties.Cors corsConfig = archForgeConfig.getCors();
        List<String> allowedOrigins = Objects.requireNonNullElse(corsConfig.getAllowedOrigins(), List.of());

        if (environment.matchesProfiles("prod") && (allowedOrigins.isEmpty() || allowedOrigins.contains("*"))) {
            throw new IllegalStateException("生产环境 CORS 必须配置具体的 allowedOrigins（arch-forge.cors.allowed-origins），不允许使用通配符 *");
        }

        config.setAllowCredentials(corsConfig.isAllowCredentials());
        if (allowedOrigins.isEmpty() || (allowedOrigins.size() == 1 && "*".equals(allowedOrigins.get(0)))) {
            config.addAllowedOriginPattern("*");
        } else {
            allowedOrigins.forEach(config::addAllowedOrigin);
        }
        corsConfig.getAllowedMethods().forEach(config::addAllowedMethod);
        corsConfig.getAllowedHeaders().forEach(config::addAllowedHeader);
        config.setExposedHeaders(corsConfig.getExposedHeaders());
        config.setMaxAge(corsConfig.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
