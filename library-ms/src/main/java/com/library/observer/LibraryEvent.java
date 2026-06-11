package com.library.observer;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents an event that occurred in the library system.
 * Part of the Observer pattern — publishers fire events, subscribers react.
 */
@Data
@Builder
public class LibraryEvent {

    public enum Type {
        BOOK_RETURNED,
        BOOK_CHECKED_OUT,
        BOOK_ADDED,
        BOOK_TRANSFERRED,
        RESERVATION_PLACED,
        LOAN_OVERDUE
    }

    private Type type;
    private String isbn;
    private String bookId;
    private String patronId;
    private String branchId;
    private String payload;
    private LocalDateTime occurredAt;
}
