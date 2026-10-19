package com.kfokam.ticket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // Origines limitées au frontend de développement (configurables via
    // app.cors.allowed-origins) : aucune identité n'est transportée, mais
    // on n'expose pas l'API à n'importe quel site.
    private final String[] originesAutorisees;

    public CorsConfig(@Value("${app.cors.allowed-origins}") String origines) {
        this.originesAutorisees = Arrays.stream(origines.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(originesAutorisees)
                .allowedMethods("GET", "POST", "PATCH", "OPTIONS");
    }
}
