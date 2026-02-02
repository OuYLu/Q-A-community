package com.community.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI communityOpenApi() {
        final String schemeName = "BearerAuth";
        return new OpenAPI()
            .info(new Info()
                .title("Smart Community API")
                .version("v1")
                .description("Smart Community backend APIs"))
            .addSecurityItem(new SecurityRequirement().addList(schemeName))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes(schemeName, new SecurityScheme()
                    .name(schemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
