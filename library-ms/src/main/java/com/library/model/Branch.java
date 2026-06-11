package com.library.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a physical library branch location.
 */
@Data
@Builder
public class Branch {

    private String branchId;
    private String name;
    private String address;
    private String phone;
    private LocalDateTime createdAt;

    @Builder.Default
    private List<String> bookIds = new ArrayList<>();

    /**
     * Adds a book copy to this branch inventory.
     */
    public void addBook(String bookId) {
        this.bookIds.add(bookId);
    }

    /**
     * Removes a book copy from this branch inventory.
     */
    public void removeBook(String bookId) {
        this.bookIds.remove(bookId);
    }

    /**
     * Returns total number of book copies held at this branch.
     */
    public int totalBooks() {
        return this.bookIds.size();
    }
}
