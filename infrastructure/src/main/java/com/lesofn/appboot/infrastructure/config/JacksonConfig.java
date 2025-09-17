package com.lesofn.appboot.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lesofn.appboot.common.utils.jackson.JsonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return JsonUtil.getObjectMapper();
    }

}