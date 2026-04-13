package com.lesofn.appforge.server.admin;

import com.lesofn.appforge.infrastructure.frame.spring.RequestContextMethodArgumentResolver;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.json.JsonMapper;

/**
 * Spring Boot 3 native configuration replacing spring-context.xml Note: Removed @EnableWebMvc to
 * allow Spring Boot auto-configuration for SpringDoc OpenAPI
 */
@Configuration
public class ApplicationConfig implements WebMvcConfigurer {

    /** Configure Jackson message converter using global JsonMapper configuration */
    @Bean
    public JacksonJsonHttpMessageConverter jacksonJsonHttpMessageConverter(JsonMapper jsonMapper) {
        JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter(jsonMapper);
        converter.setSupportedMediaTypes(
                Arrays.asList(MediaType.APPLICATION_JSON, MediaType.MULTIPART_FORM_DATA));
        return converter;
    }

    /** Configure custom argument resolvers */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new RequestContextMethodArgumentResolver());
    }

    /** Configure content negotiation to ensure proper JSON response handling */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .favorParameter(false)
                .ignoreAcceptHeader(false)
                .defaultContentType(MediaType.APPLICATION_JSON)
                .mediaType("json", MediaType.APPLICATION_JSON);
    }
}
