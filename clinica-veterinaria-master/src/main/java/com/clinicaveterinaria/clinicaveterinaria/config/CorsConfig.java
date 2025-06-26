package com.clinicaveterinaria.clinicaveterinaria.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class CorsConfig implements WebMvcConfigurer {


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica as configurações CORS a todos os endpoints da API
                .allowedOrigins("http://127.0.0.1:5500", "http://localhost:5500") // PERMITIR a origem do Live Server
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Inclua OPTIONS para preflight requests
                .allowedHeaders("*") // Permite todos os cabeçalhos nas requisições
                .allowCredentials(true) // Permite o envio de credenciais (como cookies ou cabeçalhos de autorização)
                .maxAge(3600); // Define por quanto tempo os resultados da requisição preflight podem ser armazenados em cache
    }
}

