package com.library.controller;

import com.library.dto.request.CreateBookRequest;
import com.library.dto.request.UpdateBookRequest;
import com.library.model.Book;
import com.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Book management.
 *
 * Base path: /api/books
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /** Add a new book copy to a branch. */
    @PostMapping
    public ResponseEntity<Book> addBook(@Valid @RequestBody CreateBookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.addBook(request));
    }

    /** Retrieve a single book by its unique ID. */
    @GetMapping("/{bookId}")
    public ResponseEntity<Book> getBook(@PathVariable String bookId) {
        return ResponseEntity.ok(bookService.getBookById(bookId));
    }

    /** List all books in the library. */
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    /** Update metadata on an existing book. */
    @PutMapping("/{bookId}")
    public ResponseEntity<Book> updateBook(@PathVariable String bookId,
                                           @Valid @RequestBody UpdateBookRequest request) {
        return ResponseEntity.ok(bookService.updateBook(bookId, request));
    }

    /** Remove a book copy (only allowed when not currently borrowed). */
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> removeBook(@PathVariable String bookId) {
        bookService.removeBook(bookId);
        return ResponseEntity.noContent().build();
    }

    /** Search books by title (case-insensitive, partial match). */
    @GetMapping("/search/title")
    public ResponseEntity<List<Book>> searchByTitle(@RequestParam String q) {
        return ResponseEntity.ok(bookService.searchByTitle(q));
    }

    /** Search books by author (case-insensitive, partial match). */
    @GetMapping("/search/author")
    public ResponseEntity<List<Book>> searchByAuthor(@RequestParam String q) {
        return ResponseEntity.ok(bookService.searchByAuthor(q));
    }

    /** Search books by exact ISBN. */
    @GetMapping("/search/isbn")
    public ResponseEntity<List<Book>> searchByIsbn(@RequestParam String q) {
        return ResponseEntity.ok(bookService.searchByIsbn(q));
    }

    /** List all currently available (not borrowed) book copies. */
    @GetMapping("/available")
    public ResponseEntity<List<Book>> getAvailableBooks() {
        return ResponseEntity.ok(bookService.getAvailableBooks());
    }

    /** List all currently borrowed book copies. */
    @GetMapping("/borrowed")
    public ResponseEntity<List<Book>> getBorrowedBooks() {
        return ResponseEntity.ok(bookService.getBorrowedBooks());
    }

    /** List all book copies at a specific branch. */
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<Book>> getBooksByBranch(@PathVariable String branchId) {
        return ResponseEntity.ok(bookService.getBooksByBranch(branchId));
    }
}
