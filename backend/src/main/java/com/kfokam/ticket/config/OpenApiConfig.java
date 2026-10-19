package com.kfokam.ticket.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiTickets() {
        return new OpenAPI().info(new Info()
                .title("API Tickets")
                .description("Gestion des tickets de support : création, consultation et fermeture.")
                .version("1.0"));
    }
}
