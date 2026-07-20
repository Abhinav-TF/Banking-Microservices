package com.tnf.transactionservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Registers a global "bearerAuth" (JWT) security scheme so the Swagger UI shows an
 * Authorize button. Paste the token returned by /auth/login there and every
 * "Try it out" call is sent through the gateway with an Authorization: Bearer header.
 */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "bearerAuth";

    @Bean
    public OpenAPI transactionServiceOpenAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url("/").description("API Gateway")))
                .info(new Info()
                        .title("Transaction Service API")
                        .description("Transaction ledger")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME))
                .components(new Components().addSecuritySchemes(SCHEME,
                        new SecurityScheme()
                                .name(SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
