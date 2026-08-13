package com.equipo26.financeai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/*
    Configura la documentación de la API utilizando OpenAPI
    Define los metadatos como el título, versión, descripción y datos de contacto
    que se muestran en Swagger-UI
*/
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenApi(){
        return new OpenAPI()
                .info(new Info()
                        .title("FlowFi API")
                        .description("""
                                FlowFi es un Gemelo Digital Financiero diseñado para modelar y analizar el comportamiento económico de los usuarios. A partir del historial y patrones financieros, permite simular escenarios, evaluar posibles decisiones y generar recomendaciones que ayuden a mejorar la gestión del dinero. Su objetivo es transformar datos financieros en información útil para comprender el presente, anticipar el futuro y tomar mejores decisiones económicas.
                                """)
                        .version("v1.0")
                        .contact(new Contact()
                                .name("G9-LATAM-TEAM-26")
                                .url("https://github.com/No-Country-simulation/G9-LATAM-Team-26/tree/main")
                        ))
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("DEV server"))
                .addServersItem(new Server()
                        .url("URL")
                        .description("PROD server"));
    }
}
