package com.lesofn.appengine.deploy;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.lesofn.appengine.frame.filters.AuthResourceFilter;
import com.lesofn.appengine.frame.spring.context.RequestContextMethodArgumentResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Boot 3 native configuration replacing spring-context.xml
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.lesofn.appengine")
public class ApplicationConfig implements WebMvcConfigurer {

    /**
     * Configure AuthResourceFilter bean
     */
    @Bean
    public AuthResourceFilter authResourceFilter() {
        AuthResourceFilter filter = new AuthResourceFilter();
        filter.setSynchronizeOnSession(true);
        
        // Set custom argument resolvers
        filter.setCustomArgumentResolvers(Arrays.asList(
            new RequestContextMethodArgumentResolver()
        ));
        
        // Set message converters
        filter.setMessageConverters(Arrays.asList(
            fastJsonHttpMessageConverter(),
            new StringHttpMessageConverter(),
            new ResourceHttpMessageConverter()
        ));
        
        return filter;
    }

    /**
     * Configure FastJSON message converter
     */
    @Bean
    public FastJsonHttpMessageConverter fastJsonHttpMessageConverter() {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON));
        return converter;
    }

    /**
     * Configure custom argument resolvers
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new RequestContextMethodArgumentResolver());
    }

    /**
     * Configure message converters
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(fastJsonHttpMessageConverter());
        converters.add(new StringHttpMessageConverter());
        converters.add(new ResourceHttpMessageConverter());
    }
}
