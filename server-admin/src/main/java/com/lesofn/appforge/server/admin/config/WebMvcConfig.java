package com.lesofn.appforge.server.admin.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 将 ByteArrayHttpMessageConverter 提前并支持 application/json，
 * 以确保 SpringDoc 的 byte[] OpenAPI JSON 响应以原始字节输出，不被 Jackson 序列化为 base64。
 *
 * @author sofn
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Remove existing ByteArrayHttpMessageConverter, reconfigure and add to front
        ByteArrayHttpMessageConverter byteConverter = null;
        for (int i = 0; i < converters.size(); i++) {
            if (converters.get(i) instanceof ByteArrayHttpMessageConverter) {
                byteConverter = (ByteArrayHttpMessageConverter) converters.remove(i);
                break;
            }
        }
        if (byteConverter == null) {
            byteConverter = new ByteArrayHttpMessageConverter();
        }
        // Add application/json so it can handle SpringDoc byte[] JSON responses
        List<MediaType> mediaTypes =
                new java.util.ArrayList<>(byteConverter.getSupportedMediaTypes());
        if (!mediaTypes.contains(MediaType.APPLICATION_JSON)) {
            mediaTypes.add(MediaType.APPLICATION_JSON);
        }
        byteConverter.setSupportedMediaTypes(mediaTypes);
        // Put at the front so it takes priority over Jackson for byte[] responses
        converters.addFirst(byteConverter);
    }
}
