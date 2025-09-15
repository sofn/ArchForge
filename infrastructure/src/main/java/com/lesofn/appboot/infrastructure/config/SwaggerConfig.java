package com.lesofn.appboot.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
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
	
	private final AppBootConfig appBootConfig;

	/**
	 * Configure OpenAPI specification
	 */
	@Bean
	public OpenAPI customOpenAPI() {
		// Create security scheme
		SecurityScheme securityScheme = new SecurityScheme()
			.type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT")
			.in(SecurityScheme.In.HEADER)
			.name("Authorization")
			.description("JWT token authentication");

		// Create security requirement
		SecurityRequirement securityRequirement = new SecurityRequirement()
			.addList("bearerAuth");

		// Build OpenAPI specification
		return new OpenAPI()
			.info(new Info()
				.title("AppBoot API Documentation")
				.description("RESTful API documentation for AppBoot application")
				.version(appBootConfig.getVersion())
				.contact(new Contact()
					.name(appBootConfig.getName())
					.email("support@appboot.com"))
				.license(new License()
					.name("Apache 2.0")
					.url("https://www.apache.org/licenses/LICENSE-2.0")))
			.components(new Components()
				.addSecuritySchemes("bearerAuth", securityScheme))
			.addSecurityItem(securityRequirement);
	}
}