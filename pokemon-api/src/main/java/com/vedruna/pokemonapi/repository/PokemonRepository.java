package com.vedruna.pokemonapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vedruna.pokemonapi.model.Pokemon;

public interface PokemonRepository extends JpaRepository<Pokemon, Long> {
}
