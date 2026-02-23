package com.moveinsync.mdm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mdmOpenApi() {
        final String bearerSchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("MoveInSync MDM API")
                        .description("API documentation for the Mobile Device Management backend")
                        .version("v1")
                        .contact(new Contact()
                                .name("MoveInSync Engineering")
                                .email("engineering@moveinsync.local"))
                        .license(new License()
                                .name("Internal Use")
                                .url("https://moveinsync.local")))
                .addSecurityItem(new SecurityRequirement().addList(bearerSchemeName))
                .schemaRequirement(bearerSchemeName, new SecurityScheme()
                        .name(bearerSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }
}
