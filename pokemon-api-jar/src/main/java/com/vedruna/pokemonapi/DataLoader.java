package com.vedruna.pokemonapi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.vedruna.pokemonapi.model.Pokemon;
import com.vedruna.pokemonapi.repository.PokemonRepository;

import lombok.AllArgsConstructor;

/**
 * Precarga unos pocos pokemon al arrancar, asi la API tiene datos
 * desde el primer momento sin tener que hacer POSTs a mano.
 */
@Component
@AllArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final PokemonRepository pokemonRepository;

    @Override
    public void run(String... args) {
        pokemonRepository.save(new Pokemon(null, "Pikachu", "Electrico"));
        pokemonRepository.save(new Pokemon(null, "Charmander", "Fuego"));
        pokemonRepository.save(new Pokemon(null, "Squirtle", "Agua"));
        pokemonRepository.save(new Pokemon(null, "Bulbasaur", "Planta"));
    }
}
