package com.lesofn.archforge.infrastructure.security.sign;

import com.lesofn.archforge.infrastructure.redisson.RedissonAutoConfiguration;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * API 签名自动配置。
 *
 * @author sofn
 */
@AutoConfiguration(after = RedissonAutoConfiguration.class)
@EnableConfigurationProperties(ApiSignProperties.class)
@ConditionalOnProperty(prefix = "arch-forge.security.sign", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ApiSignAutoConfiguration implements WebMvcConfigurer {

    private final RedissonClient redissonClient;
    private final ApiSignProperties apiSignProperties;
    private final ObjectProvider<ApiSignInterceptor> apiSignInterceptorProvider;

    public ApiSignAutoConfiguration(
            RedissonClient redissonClient,
            ApiSignProperties apiSignProperties,
            ObjectProvider<ApiSignInterceptor> apiSignInterceptorProvider) {
        this.redissonClient = redissonClient;
        this.apiSignProperties = apiSignProperties;
        this.apiSignInterceptorProvider = apiSignInterceptorProvider;
    }

    @Bean
    @ConditionalOnMissingBean(AppKeyProvider.class)
    public AppKeyProvider appKeyProvider() {
        return new ConfigAppKeyProvider(apiSignProperties);
    }

    @Bean
    public ApiSignInterceptor apiSignInterceptor(AppKeyProvider appKeyProvider) {
        return new ApiSignInterceptor(redissonClient, appKeyProvider, apiSignProperties);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        ApiSignInterceptor interceptor = apiSignInterceptorProvider.getIfAvailable();
        if (interceptor != null) {
            registry.addInterceptor(interceptor).addPathPatterns("/**");
        }
    }
}
