package com.vedruna.pokemonapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Version "jar ejecutable": a diferencia de la pokemon-api del .war, aqui NO hace falta
 * extender SpringBootServletInitializer. La propia aplicacion arranca su Tomcat embebido
 * con "java -jar", sin depender de ningun Tomcat instalado en la maquina.
 */
@SpringBootApplication
public class PokemonApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokemonApiApplication.class, args);
    }
}
