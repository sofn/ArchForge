package com.lesofn.matrixboot.deploy;

import com.lesofn.matrixboot.infrastructure.frame.filters.AuthResourceFilter;
import com.lesofn.matrixboot.infrastructure.frame.spring.context.RequestContextMethodArgumentResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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
@ComponentScan(basePackages = "com.lesofn.matrixboot")
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
        converters.add(jackson2HttpMessageConverter());
        converters.add(new StringHttpMessageConverter());
        converters.add(new ResourceHttpMessageConverter());
    }
}
