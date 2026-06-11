package com.library.model;

import com.library.enums.ReservationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents a patron's reservation for a currently-borrowed book.
 */
@Data
@Builder
public class Reservation {

    private String reservationId;
    private String isbn;
    private String patronId;
    private String branchId;
    private ReservationStatus status;
    private LocalDateTime reservedAt;
    private LocalDateTime notifiedAt;
    private LocalDateTime updatedAt;

    /**
     * Marks the reservation as notified (patron informed book is available).
     */
    public void markNotified() {
        this.status = ReservationStatus.NOTIFIED;
        this.notifiedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marks the reservation as fulfilled after checkout.
     */
    public void markFulfilled() {
        this.status = ReservationStatus.FULFILLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Cancels this reservation.
     */
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
}
