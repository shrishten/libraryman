package com.library.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a library patron (member).
 * Tracks borrowing history and active loans.
 */
@Data
@Builder
public class Patron {

    private String patronId;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime memberSince;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<String> borrowingHistory = new ArrayList<>();   // bookIds

    @Builder.Default
    private List<String> activeLoans = new ArrayList<>();        // loanIds

    @Builder.Default
    private List<String> reservations = new ArrayList<>();       // reservationIds

    @Builder.Default
    private List<String> preferredGenres = new ArrayList<>();

    /**
     * Records a completed loan in borrowing history.
     */
    public void addToBorrowingHistory(String bookId) {
        this.borrowingHistory.add(bookId);
    }

    /**
     * Returns total number of books ever borrowed (for recommendation scoring).
     */
    public int totalBooksBorrowed() {
        return this.borrowingHistory.size();
    }
}
