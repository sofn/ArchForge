package com.lesofn.archsmith.server.admin.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 将 ByteArrayHttpMessageConverter 提前并支持 application/json， 以确保 SpringDoc 的 byte[] OpenAPI JSON
 * 响应以原始字节输出，不被 Jackson 序列化为 base64。
 *
 * @author sofn
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        builder.registerDefaults();
        builder.configureMessageConvertersList(
                converters -> {
                    // Add a ByteArrayHttpMessageConverter with JSON support at the front
                    ByteArrayHttpMessageConverter byteConverter =
                            new ByteArrayHttpMessageConverter();
                    List<MediaType> mediaTypes =
                            new java.util.ArrayList<>(byteConverter.getSupportedMediaTypes());
                    if (!mediaTypes.contains(MediaType.APPLICATION_JSON)) {
                        mediaTypes.add(MediaType.APPLICATION_JSON);
                    }
                    byteConverter.setSupportedMediaTypes(mediaTypes);
                    converters.addFirst(byteConverter);
                });
    }
}
