package com.lesofn.archforge.server.web.config;

import com.lesofn.archforge.server.web.interceptor.WebAuthInterceptor;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;
    private final WebAuthInterceptor webAuthInterceptor;

    @Bean
    public JacksonJsonHttpMessageConverter jacksonJsonHttpMessageConverter() {
        JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter((JsonMapper) objectMapper);
        converter.setSupportedMediaTypes(
                Arrays.asList(MediaType.APPLICATION_JSON, MediaType.MULTIPART_FORM_DATA));
        return converter;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        boolean has = converters.stream()
                .anyMatch(c -> c instanceof JacksonJsonHttpMessageConverter);
        if (!has) {
            converters.add(0, jacksonJsonHttpMessageConverter());
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(webAuthInterceptor)
                .addPathPatterns("/web/**")
                .excludePathPatterns("/web/login", "/web/refresh-token", "/web/categories");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
