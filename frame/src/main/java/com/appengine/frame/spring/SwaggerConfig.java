package com.appengine.frame.spring;

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

    /*@Bean
    public Docket merchantStoreApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("api")
                .genericModelSubstitutes(DeferredResult.class)
                .useDefaultResponseMessages(false)
                .forCodeGeneration(true)
                .pathMapping("/")// base，最终调用接口后会和paths拼接在一起
                .select()
                .apis(RequestHandlerSelectors.any()) // 对所有api进行监控
                .paths(PathSelectors.any()) // 对所有路径进行监控
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("APP-ENGINE接口文档")
                .description("文档说明")
                .contact(new Contact("sofn", "https://github.com/sofn", "lesofn@gmail.com"))
                .version("1.0")
                .build();
    }*/
}