package com.library.controller;

import com.library.dto.request.CreatePatronRequest;
import com.library.dto.request.UpdatePatronRequest;
import com.library.model.Loan;
import com.library.model.Patron;
import com.library.service.PatronService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Patron (library member) management.
 *
 * Base path: /api/patrons
 */
@RestController
@RequestMapping("/api/patrons")
public class PatronController {

    private final PatronService patronService;

    public PatronController(PatronService patronService) {
        this.patronService = patronService;
    }

    /** Register a new library patron. */
    @PostMapping
    public ResponseEntity<Patron> registerPatron(@Valid @RequestBody CreatePatronRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patronService.registerPatron(request));
    }

    /** Retrieve a single patron by ID. */
    @GetMapping("/{patronId}")
    public ResponseEntity<Patron> getPatron(@PathVariable String patronId) {
        return ResponseEntity.ok(patronService.getPatronById(patronId));
    }

    /** List all registered patrons. */
    @GetMapping
    public ResponseEntity<List<Patron>> getAllPatrons() {
        return ResponseEntity.ok(patronService.getAllPatrons());
    }

    /** Update a patron's profile information. */
    @PutMapping("/{patronId}")
    public ResponseEntity<Patron> updatePatron(@PathVariable String patronId,
                                               @Valid @RequestBody UpdatePatronRequest request) {
        return ResponseEntity.ok(patronService.updatePatron(patronId, request));
    }

    /** Get full borrowing history (all loans) for a patron. */
    @GetMapping("/{patronId}/history")
    public ResponseEntity<List<Loan>> getBorrowingHistory(@PathVariable String patronId) {
        return ResponseEntity.ok(patronService.getBorrowingHistory(patronId));
    }

    /** Get only currently active loans for a patron. */
    @GetMapping("/{patronId}/loans")
    public ResponseEntity<List<Loan>> getActiveLoans(@PathVariable String patronId) {
        return ResponseEntity.ok(patronService.getActiveLoans(patronId));
    }
}
