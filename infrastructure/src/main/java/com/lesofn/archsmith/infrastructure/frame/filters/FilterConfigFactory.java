package com.lesofn.archsmith.infrastructure.frame.filters;

import io.micrometer.observation.ObservationRegistry;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import java.util.EnumSet;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Authors: sofn Version: 1.0 Created at 15-6-8 23:15. */
@Configuration
@RequiredArgsConstructor
public class FilterConfigFactory implements WebMvcConfigurer {

    private final ObservationRegistry observationRegistry;

    @Bean
    public FilterRegistrationBean<Filter> requestLogChain() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        Filter headerFilter = new RequestLogFilter(observationRegistry);
        registration.setFilter(headerFilter);
        registration.setOrder(Integer.MAX_VALUE);
        // 拦截错误转发
        registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
        return registration;
    }

    @Bean
    public FilterRegistrationBean<Filter> headerFilterChain() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        Filter headerFilter = new HeaderResponseFilter();
        registration.setFilter(headerFilter);
        registration.setOrder(Integer.MAX_VALUE);
        return registration;
    }
}
