package com.lesofn.appboot.server.admin;

import com.google.common.collect.ImmutableList;
import com.lesofn.appboot.infrastructure.frame.spring.RequestContextMethodArgumentResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Boot 3 native configuration replacing spring-context.xml
 * Note: Removed @EnableWebMvc to allow Spring Boot auto-configuration for SpringDoc OpenAPI
 */
@Configuration
public class ApplicationConfig implements WebMvcConfigurer {

    /**
     * Configure AuthResourceFilter bean
     */
    @Bean
    public RequestMappingHandlerAdapter filterConfig() {
        RequestMappingHandlerAdapter filter = new RequestMappingHandlerAdapter();
        filter.setSynchronizeOnSession(true);

        // Set custom argument resolvers
        filter.setCustomArgumentResolvers(ImmutableList.of(
                new RequestContextMethodArgumentResolver()
        ));

        // Set message converters
        filter.setMessageConverters(Arrays.asList(
                jackson2HttpMessageConverter(),
                new StringHttpMessageConverter(),
                new ResourceHttpMessageConverter()
        ));

        return filter;
    }

    /**
     * Configure Jackson message converter
     */
    @Bean
    public MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_JSON,
                new MediaType("application", "json", java.nio.charset.StandardCharsets.UTF_8),
                MediaType.MULTIPART_FORM_DATA
        ));
        return converter;
    }

    /**
     * Configure custom argument resolvers
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new RequestContextMethodArgumentResolver());
    }

}
