package com.lesofn.matrixboot.frame.filters;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import java.util.EnumSet;

/**
 * Authors: sofn
 * Version: 1.0  Created at 15-6-8 23:15.
 */
@Configuration
public class FilterConfigFactory implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean requestLogChain() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        Filter headerFilter = new RequestLogFilter();
        registration.setFilter(headerFilter);
        registration.setOrder(Integer.MAX_VALUE);
        //拦截错误转发
        registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
        return registration;
    }

    @Bean
    public FilterRegistrationBean headerFilterChain() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        Filter headerFilter = new HeaderResponseFilter();
        registration.setFilter(headerFilter);
        registration.setOrder(Integer.MAX_VALUE);
        return registration;
    }

}
