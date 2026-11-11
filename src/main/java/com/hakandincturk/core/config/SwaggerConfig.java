package com.hakandincturk.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("/").description("Default Server"))
                .info(new Info()
                        .title("MyMoney API")
                        .description("MyMoney Application REST API Documentation")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Hakan Dinçtürk")
                                .email("hakandincturk@example.com")))
                .addSecurityItem(new SecurityRequirement().addList("sessionCookie"))
                .components(new Components()
                        .addSecuritySchemes("sessionCookie",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("mm_session")));
    }
}