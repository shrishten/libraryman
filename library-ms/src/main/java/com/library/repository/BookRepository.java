package com.library.repository;

import com.library.enums.BookStatus;
import com.library.model.Book;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory repository for Book entities.
 * Uses ConcurrentHashMap for thread-safe access.
 */
@Repository
public class BookRepository {

    private final Map<String, Book> store = new ConcurrentHashMap<>();

    public Book save(Book book) {
        store.put(book.getBookId(), book);
        return book;
    }

    public Optional<Book> findById(String bookId) {
        return Optional.ofNullable(store.get(bookId));
    }

    public List<Book> findAll() {
        return new ArrayList<>(store.values());
    }

    public void deleteById(String bookId) {
        store.remove(bookId);
    }

    public boolean existsById(String bookId) {
        return store.containsKey(bookId);
    }

    // ── Search methods ─────────────────────────────────────────────────────────

    public List<Book> findByTitleContainingIgnoreCase(String title) {
        return store.values().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Book> findByAuthorContainingIgnoreCase(String author) {
        return store.values().stream()
                .filter(b -> b.getAuthor().toLowerCase().contains(author.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Book> findByIsbn(String isbn) {
        return store.values().stream()
                .filter(b -> b.getIsbn().equals(isbn))
                .collect(Collectors.toList());
    }

    public List<Book> findByStatus(BookStatus status) {
        return store.values().stream()
                .filter(b -> status.equals(b.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Book> findByBranchId(String branchId) {
        return store.values().stream()
                .filter(b -> branchId.equals(b.getBranchId()))
                .collect(Collectors.toList());
    }

    public Optional<Book> findFirstAvailableByIsbn(String isbn) {
        return store.values().stream()
                .filter(b -> b.getIsbn().equals(isbn) && b.isAvailable())
                .findFirst();
    }
}
