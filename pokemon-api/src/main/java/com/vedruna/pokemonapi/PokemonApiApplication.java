package com.vedruna.pokemonapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class PokemonApiApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(PokemonApiApplication.class, args);
    }

    // Necesario para que un Tomcat externo (Tema 2) sepa arrancar el contexto de Spring
    // al desplegar el .war. No afecta a "mvn spring-boot:run" en local.
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(PokemonApiApplication.class);
    }
}
