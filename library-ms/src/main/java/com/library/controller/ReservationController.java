package com.library.controller;

import com.library.dto.request.ReservationRequest;
import com.library.model.Reservation;
import com.library.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for book reservation management.
 *
 * Base path: /api/reservations
 */
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /** Place a reservation for a currently borrowed book. */
    @PostMapping
    public ResponseEntity<Reservation> placeReservation(@Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.placeReservation(request));
    }

    /** Cancel an existing reservation. */
    @DeleteMapping("/{reservationId}/patron/{patronId}")
    public ResponseEntity<Reservation> cancelReservation(@PathVariable String reservationId,
                                                          @PathVariable String patronId) {
        return ResponseEntity.ok(reservationService.cancelReservation(reservationId, patronId));
    }

    /** List all reservations (any status) for a patron. */
    @GetMapping("/patron/{patronId}")
    public ResponseEntity<List<Reservation>> getByPatron(@PathVariable String patronId) {
        return ResponseEntity.ok(reservationService.getReservationsByPatron(patronId));
    }

    /** List pending reservations for a given ISBN. */
    @GetMapping("/isbn/{isbn}/pending")
    public ResponseEntity<List<Reservation>> getPendingByIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(reservationService.getPendingReservationsByIsbn(isbn));
    }
}
