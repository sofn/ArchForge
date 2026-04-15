package com.lesofn.archsmith.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI Configuration
 *
 * @author sofn
 * @version 1.0 Created at: 2016-10-18 20:10
 */
@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private final ArchSmithConfig appForgeConfig;

    @Bean
    public OpenAPI initOpenAPI() {
        return new OpenAPI()
                .openapi("3.1.0")
                .components(new Components().addSecuritySchemes("apikey", securityScheme()))
                .addSecurityItem(new SecurityRequirement().addList("apikey"))
                .info(getApiInfo());
    }

    @Bean
    public SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .name("Authorization")
                .in(SecurityScheme.In.HEADER)
                .scheme("Bearer");
    }

    /** 添加摘要信息 */
    public Info getApiInfo() {
        return new Info()
                // 设置标题
                .title("标题：ArchSmith接口文档")
                // 描述
                .description("描述：文档说明")
                // 作者信息
                .contact(new Contact().name(appForgeConfig.getName()))
                // 版本
                .version("版本号:" + appForgeConfig.getVersion());
    }
}
