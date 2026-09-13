package com.lesofn.archforge.starter.requestlog;

import jakarta.servlet.DispatcherType;
import java.util.EnumSet;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * 请求日志 starter 装配：注册 {@link RequestLogFilter}，收集全部
 * {@link RequestLogEnricher} 实现按序增强日志记录。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(RequestLogProperties.class)
public class RequestLogAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "arch-forge.request-log", name = "enabled", havingValue = "true",
            matchIfMissing = true)
    public FilterRegistrationBean<RequestLogFilter> requestLogPayloadFilter(RequestLogProperties properties,
            ObjectProvider<RequestLogEnricher> enrichers) {
        FilterRegistrationBean<RequestLogFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestLogFilter(properties, enrichers, new LoggingRequestLogSink(properties
                .getMaxPayloadLength())));
        registration.addUrlPatterns("/*");
        registration.setName("requestLogPayloadFilter");
        // 覆盖 /error 转发（filter 内部按原 URI 回填 api 字段）
        registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
        registration.setOrder(properties.getOrder());
        return registration;
    }
}
