package com.mansa.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankPayXOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BankPayX Mobile Money API")
                        .description("API de paiement Mobile Money — MTN, Orange, Wave, CinetPay")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BankPayX Engineering")
                                .email("tech@bankpayx.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://bankpayx.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local"),
                        new Server().url("https://api.bankpayx.com").description("Production")))
                // Déclare le schéma de sécurité JWT Bearer
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .name("BearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Coller le JWT obtenu depuis Keycloak")));
    }
}
