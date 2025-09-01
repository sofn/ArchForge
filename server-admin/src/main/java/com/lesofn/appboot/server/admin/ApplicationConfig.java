package com.lesofn.appboot.server.admin;

import com.lesofn.appboot.infrastructure.frame.filters.AuthResourceFilter;
import com.lesofn.appboot.infrastructure.frame.spring.context.RequestContextMethodArgumentResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.MultipartConfigElement;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Boot 3 native configuration replacing spring-context.xml
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.lesofn.appboot")
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
        converter.setSupportedMediaTypes(Arrays.asList(
            MediaType.APPLICATION_JSON,
            MediaType.MULTIPART_FORM_DATA
        ));
        return converter;
    }

    /**
     * Configure multipart resolver for file uploads
     */
    @Bean
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        resolver.setResolveLazily(true);
        return resolver;
    }

    /**
     * Configure MultipartConfigElement with proper limits
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        // Define upload parameters
        String uploadTempDir = System.getProperty("java.io.tmpdir");
        File uploadDirectory = new File(uploadTempDir);
        
        // Ensure temp directory exists
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }
        
        // 10MB max file size, 20MB max request size
        long maxFileSize = 10 * 1024 * 1024; 
        long maxRequestSize = 20 * 1024 * 1024;
        int fileSizeThreshold = 0;
        
        return new MultipartConfigElement(
            uploadTempDir, 
            maxFileSize, 
            maxRequestSize, 
            fileSizeThreshold
        );
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
