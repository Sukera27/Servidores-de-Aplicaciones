package com.vedruna.pokemonapi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vedruna.pokemonapi.model.Pokemon;
import com.vedruna.pokemonapi.repository.PokemonRepository;

import lombok.AllArgsConstructor;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/pokemon")
@AllArgsConstructor
public class PokemonController {

    private final PokemonRepository pokemonRepository;

    @GetMapping
    public List<Pokemon> getAll() {
        return pokemonRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pokemon> getById(@PathVariable Long id) {
        return pokemonRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public Pokemon create(@RequestBody Pokemon pokemon) {
        pokemon.setId(null); // el id siempre lo genera la base de datos
        return pokemonRepository.save(pokemon);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pokemon> update(@PathVariable Long id, @RequestBody Pokemon datos) {
        return pokemonRepository.findById(id)
                .map(pokemon -> {
                    pokemon.setNombre(datos.getNombre());
                    pokemon.setTipo(datos.getTipo());
                    return ResponseEntity.ok(pokemonRepository.save(pokemon));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!pokemonRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        pokemonRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
