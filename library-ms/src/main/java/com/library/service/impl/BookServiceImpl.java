package com.library.service.impl;

import com.library.dto.request.CreateBookRequest;
import com.library.dto.request.UpdateBookRequest;
import com.library.enums.BookStatus;
import com.library.exception.BusinessException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Book;
import com.library.observer.LibraryEvent;
import com.library.observer.LibraryEventPublisher;
import com.library.repository.BookRepository;
import com.library.repository.BranchRepository;
import com.library.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookServiceImpl implements BookService {

    private static final Logger log = LoggerFactory.getLogger(BookServiceImpl.class);

    private final BookRepository bookRepository;
    private final BranchRepository branchRepository;
    private final LibraryEventPublisher eventPublisher;

    public BookServiceImpl(BookRepository bookRepository,
                           BranchRepository branchRepository,
                           LibraryEventPublisher eventPublisher) {
        this.bookRepository = bookRepository;
        this.branchRepository = branchRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Book addBook(CreateBookRequest request) {
        if (!branchRepository.existsById(request.getBranchId())) {
            throw new ResourceNotFoundException("Branch not found: " + request.getBranchId());
        }

        Book book = Book.builder()
                .bookId(UUID.randomUUID().toString())
                .isbn(request.getIsbn())
                .title(request.getTitle())
                .author(request.getAuthor())
                .publicationYear(request.getPublicationYear())
                .genre(request.getGenre())
                .branchId(request.getBranchId())
                .status(BookStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        bookRepository.save(book);

        branchRepository.findById(request.getBranchId())
                .ifPresent(branch -> {
                    branch.addBook(book.getBookId());
                    branchRepository.save(branch);
                });

        eventPublisher.publish(LibraryEvent.builder()
                .type(LibraryEvent.Type.BOOK_ADDED)
                .bookId(book.getBookId())
                .isbn(book.getIsbn())
                .branchId(book.getBranchId())
                .occurredAt(LocalDateTime.now())
                .build());

        log.info("Book added: id={}, isbn={}, title={}", book.getBookId(), book.getIsbn(), book.getTitle());
        return book;
    }

    @Override
    public Book getBookById(String bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + bookId));
    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Book updateBook(String bookId, UpdateBookRequest request) {
        Book book = getBookById(bookId);

        if (request.getTitle() != null)           book.setTitle(request.getTitle());
        if (request.getAuthor() != null)          book.setAuthor(request.getAuthor());
        if (request.getPublicationYear() != null) book.setPublicationYear(request.getPublicationYear());
        if (request.getGenre() != null)           book.setGenre(request.getGenre());
        book.setUpdatedAt(LocalDateTime.now());

        bookRepository.save(book);
        log.info("Book updated: id={}", bookId);
        return book;
    }

    @Override
    public void removeBook(String bookId) {
        Book book = getBookById(bookId);

        if (BookStatus.BORROWED.equals(book.getStatus())) {
            throw new BusinessException("Cannot remove a book that is currently borrowed.");
        }

        branchRepository.findById(book.getBranchId())
                .ifPresent(branch -> {
                    branch.removeBook(bookId);
                    branchRepository.save(branch);
                });

        bookRepository.deleteById(bookId);
        log.info("Book removed: id={}", bookId);
    }

    @Override
    public List<Book> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    @Override
    public List<Book> searchByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }

    @Override
    public List<Book> searchByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    @Override
    public List<Book> getAvailableBooks() {
        return bookRepository.findByStatus(BookStatus.AVAILABLE);
    }

    @Override
    public List<Book> getBorrowedBooks() {
        return bookRepository.findByStatus(BookStatus.BORROWED);
    }

    @Override
    public List<Book> getBooksByBranch(String branchId) {
        if (!branchRepository.existsById(branchId)) {
            throw new ResourceNotFoundException("Branch not found: " + branchId);
        }
        return bookRepository.findByBranchId(branchId);
    }
}
