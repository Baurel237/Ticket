package com.kfokam.ticket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // Le frontend peut être ouvert via localhost, 127.0.0.1 ou l'adresse réseau :
    // on accepte toute origine pour l'API (aucun cookie ni identifiant n'est utilisé).
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PATCH", "OPTIONS");
    }
}
