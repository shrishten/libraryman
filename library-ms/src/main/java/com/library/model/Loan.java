package com.library.model;

import com.library.enums.LoanStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a book loan transaction between a patron and the library.
 */
@Data
@Builder
public class Loan {

    private String loanId;
    private String bookId;
    private String patronId;
    private String branchId;
    private LocalDate checkoutDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private LoanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Marks the loan as returned today.
     */
    public void markReturned() {
        this.returnDate = LocalDate.now();
        this.status = LoanStatus.RETURNED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if this loan is overdue.
     */
    public boolean isOverdue() {
        return LoanStatus.ACTIVE.equals(this.status)
                && LocalDate.now().isAfter(this.dueDate);
    }
}
