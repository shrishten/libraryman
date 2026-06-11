package com.library.service;

import com.library.dto.request.CreateBookRequest;
import com.library.dto.request.UpdateBookRequest;
import com.library.model.Book;

import java.util.List;

/**
 * Service contract for book management operations.
 * Follows Interface Segregation and Dependency Inversion principles.
 */
public interface BookService {

    Book addBook(CreateBookRequest request);

    Book getBookById(String bookId);

    List<Book> getAllBooks();

    Book updateBook(String bookId, UpdateBookRequest request);

    void removeBook(String bookId);

    List<Book> searchByTitle(String title);

    List<Book> searchByAuthor(String author);

    List<Book> searchByIsbn(String isbn);

    List<Book> getAvailableBooks();

    List<Book> getBorrowedBooks();

    List<Book> getBooksByBranch(String branchId);
}
