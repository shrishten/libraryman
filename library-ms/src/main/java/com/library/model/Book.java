package com.library.model;

import com.library.enums.BookStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents a physical book copy in the library.
 * Each Book has a unique bookId and belongs to a Branch.
 */
@Data
@Builder
public class Book {

    private String bookId;
    private String isbn;
    private String title;
    private String author;
    private int publicationYear;
    private String genre;
    private String branchId;
    private BookStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Returns true if this book copy is available for checkout or reservation.
     */
    public boolean isAvailable() {
        return BookStatus.AVAILABLE.equals(this.status);
    }
}
