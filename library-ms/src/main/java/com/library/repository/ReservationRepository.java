package com.library.repository;

import com.library.enums.ReservationStatus;
import com.library.model.Reservation;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory repository for Reservation entities.
 */
@Repository
public class ReservationRepository {

    private final Map<String, Reservation> store = new ConcurrentHashMap<>();

    public Reservation save(Reservation reservation) {
        store.put(reservation.getReservationId(), reservation);
        return reservation;
    }

    public Optional<Reservation> findById(String reservationId) {
        return Optional.ofNullable(store.get(reservationId));
    }

    public List<Reservation> findAll() {
        return new ArrayList<>(store.values());
    }

    public List<Reservation> findByPatronId(String patronId) {
        return store.values().stream()
                .filter(r -> patronId.equals(r.getPatronId()))
                .collect(Collectors.toList());
    }

    public List<Reservation> findByIsbnAndStatus(String isbn, ReservationStatus status) {
        return store.values().stream()
                .filter(r -> isbn.equals(r.getIsbn()) && status.equals(r.getStatus()))
                .sorted(Comparator.comparing(Reservation::getReservedAt))
                .collect(Collectors.toList());
    }

    public boolean existsByPatronIdAndIsbnAndStatus(String patronId, String isbn, ReservationStatus status) {
        return store.values().stream()
                .anyMatch(r -> patronId.equals(r.getPatronId())
                        && isbn.equals(r.getIsbn())
                        && status.equals(r.getStatus()));
    }
}
