package com.equipo26.financeai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
    Habilita CORS para que el frontend pueda llamar a esta API.

    El origen permitido se lee de la propiedad "app.cors.allowed-origins"
    (variable de entorno CORS_ALLOWED_ORIGINS en producción). Por defecto
    permite todo ("*") para que la demo del hackathon funcione sin fricción;
    en producción real conviene restringirlo al dominio real del frontend.
*/
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
