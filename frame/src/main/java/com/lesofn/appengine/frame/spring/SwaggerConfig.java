package com.lesofn.appengine.frame.spring;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author sofn
 * @version 1.0 Created at: 2016-10-18 20:10
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI initOpenAPI() {
        return new OpenAPI().info(
                new Info().title("APP-ENGINE接口文档").description("文档说明").version("v1.0")
        );
    }
}