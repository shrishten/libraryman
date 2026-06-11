package com.library.service;

import com.library.dto.request.CreateBookRequest;
import com.library.dto.request.UpdateBookRequest;
import com.library.enums.BookStatus;
import com.library.exception.BusinessException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Book;
import com.library.model.Branch;
import com.library.observer.LibraryEventPublisher;
import com.library.repository.BookRepository;
import com.library.repository.BranchRepository;
import com.library.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceTest {

    private BookRepository bookRepository;
    private BranchRepository branchRepository;
    private LibraryEventPublisher eventPublisher;
    private BookService bookService;

    private static final String BRANCH_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        bookRepository   = mock(BookRepository.class);
        branchRepository = mock(BranchRepository.class);
        eventPublisher   = mock(LibraryEventPublisher.class);
        bookService = new BookServiceImpl(bookRepository, branchRepository, eventPublisher);
    }

    @Test
    @DisplayName("addBook: persists book and returns it with AVAILABLE status")
    void addBook_success() {
        when(branchRepository.existsById(BRANCH_ID)).thenReturn(true);
        when(branchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(
                Branch.builder().branchId(BRANCH_ID).name("Central").address("1 Main").createdAt(LocalDateTime.now()).build()));
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateBookRequest request = new CreateBookRequest();
        request.setIsbn("978-0-06-112008-4");
        request.setTitle("To Kill a Mockingbird");
        request.setAuthor("Harper Lee");
        request.setPublicationYear(1960);
        request.setGenre("Fiction");
        request.setBranchId(BRANCH_ID);

        Book result = bookService.addBook(request);

        assertThat(result.getTitle()).isEqualTo("To Kill a Mockingbird");
        assertThat(result.getStatus()).isEqualTo(BookStatus.AVAILABLE);
        assertThat(result.getBookId()).isNotNull();
        verify(bookRepository).save(any());
        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("addBook: throws ResourceNotFoundException when branch does not exist")
    void addBook_branchNotFound() {
        when(branchRepository.existsById(anyString())).thenReturn(false);

        CreateBookRequest request = new CreateBookRequest();
        request.setIsbn("123"); request.setTitle("T"); request.setAuthor("A");
        request.setPublicationYear(2000); request.setBranchId("bad-branch");

        assertThatThrownBy(() -> bookService.addBook(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Branch not found");
    }

    @Test
    @DisplayName("removeBook: throws BusinessException when book is currently borrowed")
    void removeBook_borrowed_throws() {
        Book borrowed = Book.builder()
                .bookId("b1").isbn("x").title("T").author("A")
                .publicationYear(2000).branchId(BRANCH_ID)
                .status(BookStatus.BORROWED).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(bookRepository.findById("b1")).thenReturn(Optional.of(borrowed));

        assertThatThrownBy(() -> bookService.removeBook("b1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("currently borrowed");
    }

    @Test
    @DisplayName("updateBook: applies partial field updates correctly")
    void updateBook_partialUpdate() {
        Book existing = Book.builder()
                .bookId("b1").isbn("x").title("Old Title").author("Old Author")
                .publicationYear(2000).branchId(BRANCH_ID)
                .status(BookStatus.AVAILABLE).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(bookRepository.findById("b1")).thenReturn(Optional.of(existing));
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookRequest update = new UpdateBookRequest();
        update.setTitle("New Title");

        Book result = bookService.updateBook("b1", update);

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getAuthor()).isEqualTo("Old Author"); // unchanged
    }

    @Test
    @DisplayName("searchByTitle: returns books matching partial title, case-insensitive")
    void searchByTitle_returnsMatches() {
        Book book = Book.builder().bookId("b1").isbn("x").title("The Great Gatsby")
                .author("Fitzgerald").publicationYear(1925).branchId(BRANCH_ID)
                .status(BookStatus.AVAILABLE).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(bookRepository.findByTitleContainingIgnoreCase("gatsby")).thenReturn(List.of(book));

        List<Book> results = bookService.searchByTitle("gatsby");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).containsIgnoringCase("gatsby");
    }

    @Test
    @DisplayName("getBookById: throws ResourceNotFoundException for unknown ID")
    void getBookById_notFound() {
        when(bookRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById("unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
