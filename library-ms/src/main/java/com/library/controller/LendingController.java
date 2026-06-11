package com.library.controller;

import com.library.dto.request.CheckoutRequest;
import com.library.model.Loan;
import com.library.service.LendingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the book lending process (checkout and return).
 *
 * Base path: /api/loans
 */
@RestController
@RequestMapping("/api/loans")
public class LendingController {

    private final LendingService lendingService;

    public LendingController(LendingService lendingService) {
        this.lendingService = lendingService;
    }

    /** Check out an available book for a patron. */
    @PostMapping("/checkout")
    public ResponseEntity<Loan> checkout(@Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lendingService.checkoutBook(request));
    }

    /** Return a borrowed book — triggers reservation notifications automatically. */
    @PostMapping("/{loanId}/return")
    public ResponseEntity<Loan> returnBook(@PathVariable String loanId) {
        return ResponseEntity.ok(lendingService.returnBook(loanId));
    }

    /** Retrieve details of a specific loan. */
    @GetMapping("/{loanId}")
    public ResponseEntity<Loan> getLoan(@PathVariable String loanId) {
        return ResponseEntity.ok(lendingService.getLoanById(loanId));
    }
}
