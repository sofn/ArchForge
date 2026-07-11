package com.lesofn.archforge.infrastructure.config;

import tools.jackson.databind.ObjectMapper;
import com.lesofn.archforge.common.utils.jackson.JsonUtil;
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
