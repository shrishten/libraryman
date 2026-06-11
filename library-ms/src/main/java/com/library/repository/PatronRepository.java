package com.library.repository;

import com.library.model.Patron;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory repository for Patron entities.
 */
@Repository
public class PatronRepository {

    private final Map<String, Patron> store = new ConcurrentHashMap<>();

    public Patron save(Patron patron) {
        store.put(patron.getPatronId(), patron);
        return patron;
    }

    public Optional<Patron> findById(String patronId) {
        return Optional.ofNullable(store.get(patronId));
    }

    public List<Patron> findAll() {
        return new ArrayList<>(store.values());
    }

    public void deleteById(String patronId) {
        store.remove(patronId);
    }

    public boolean existsById(String patronId) {
        return store.containsKey(patronId);
    }

    public Optional<Patron> findByEmail(String email) {
        return store.values().stream()
                .filter(p -> p.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public List<Patron> findByNameContainingIgnoreCase(String name) {
        return store.values().stream()
                .filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }
}
