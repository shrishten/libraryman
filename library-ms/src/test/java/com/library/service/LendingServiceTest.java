package com.library.service;

import com.library.dto.request.CheckoutRequest;
import com.library.enums.BookStatus;
import com.library.enums.LoanStatus;
import com.library.exception.BusinessException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Patron;
import com.library.observer.LibraryEventPublisher;
import com.library.repository.*;
import com.library.service.impl.LendingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LendingServiceTest {

    private BookRepository bookRepository;
    private PatronRepository patronRepository;
    private LoanRepository loanRepository;
    private BranchRepository branchRepository;
    private ReservationRepository reservationRepository;
    private LibraryEventPublisher eventPublisher;
    private LendingService lendingService;

    private static final String PATRON_ID = UUID.randomUUID().toString();
    private static final String BRANCH_ID = UUID.randomUUID().toString();
    private static final String ISBN      = "978-0-7432-7356-5";

    private Patron samplePatron;
    private Book   sampleBook;

    @BeforeEach
    void setUp() {
        bookRepository        = mock(BookRepository.class);
        patronRepository      = mock(PatronRepository.class);
        loanRepository        = mock(LoanRepository.class);
        branchRepository      = mock(BranchRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        eventPublisher        = mock(LibraryEventPublisher.class);

        lendingService = new LendingServiceImpl(
                bookRepository, patronRepository, loanRepository,
                branchRepository, reservationRepository, eventPublisher);

        samplePatron = Patron.builder()
                .patronId(PATRON_ID).name("Alice").email("alice@test.com")
                .memberSince(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        sampleBook = Book.builder()
                .bookId(UUID.randomUUID().toString()).isbn(ISBN)
                .title("1984").author("George Orwell").publicationYear(1949)
                .branchId(BRANCH_ID).status(BookStatus.AVAILABLE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("checkoutBook: creates ACTIVE loan and marks book BORROWED")
    void checkout_success() {
        when(patronRepository.findById(PATRON_ID)).thenReturn(Optional.of(samplePatron));
        when(branchRepository.existsById(BRANCH_ID)).thenReturn(true);
        when(bookRepository.findByIsbn(ISBN)).thenReturn(List.of(sampleBook));
        when(loanRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(patronRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reservationRepository.findByIsbnAndStatus(any(), any())).thenReturn(List.of());

        CheckoutRequest request = new CheckoutRequest();
        request.setPatronId(PATRON_ID);
        request.setIsbn(ISBN);
        request.setBranchId(BRANCH_ID);

        Loan loan = lendingService.checkoutBook(request);

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(loan.getPatronId()).isEqualTo(PATRON_ID);
        assertThat(loan.getDueDate()).isEqualTo(LocalDate.now().plusDays(14));
        assertThat(sampleBook.getStatus()).isEqualTo(BookStatus.BORROWED);
        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("checkoutBook: throws BusinessException when no available copy exists")
    void checkout_noAvailableCopy() {
        sampleBook.setStatus(BookStatus.BORROWED);
        when(patronRepository.findById(PATRON_ID)).thenReturn(Optional.of(samplePatron));
        when(branchRepository.existsById(BRANCH_ID)).thenReturn(true);
        when(bookRepository.findByIsbn(ISBN)).thenReturn(List.of(sampleBook));

        CheckoutRequest request = new CheckoutRequest();
        request.setPatronId(PATRON_ID);
        request.setIsbn(ISBN);
        request.setBranchId(BRANCH_ID);

        assertThatThrownBy(() -> lendingService.checkoutBook(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No available copy");
    }

    @Test
    @DisplayName("returnBook: marks loan RETURNED and book AVAILABLE")
    void returnBook_success() {
        Loan activeLoan = Loan.builder()
                .loanId("loan1").bookId(sampleBook.getBookId())
                .patronId(PATRON_ID).branchId(BRANCH_ID)
                .checkoutDate(LocalDate.now().minusDays(5))
                .dueDate(LocalDate.now().plusDays(9))
                .status(LoanStatus.ACTIVE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(loanRepository.findById("loan1")).thenReturn(Optional.of(activeLoan));
        when(bookRepository.findById(sampleBook.getBookId())).thenReturn(Optional.of(sampleBook));
        when(loanRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(patronRepository.findById(PATRON_ID)).thenReturn(Optional.of(samplePatron));
        when(patronRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Loan result = lendingService.returnBook("loan1");

        assertThat(result.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(result.getReturnDate()).isEqualTo(LocalDate.now());
        assertThat(sampleBook.getStatus()).isEqualTo(BookStatus.AVAILABLE);
        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("returnBook: throws BusinessException when loan is not active")
    void returnBook_alreadyReturned() {
        Loan returned = Loan.builder()
                .loanId("loan1").bookId("b1").patronId(PATRON_ID).branchId(BRANCH_ID)
                .status(LoanStatus.RETURNED)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(loanRepository.findById("loan1")).thenReturn(Optional.of(returned));

        assertThatThrownBy(() -> lendingService.returnBook("loan1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not active");
    }

    @Test
    @DisplayName("checkoutBook: throws ResourceNotFoundException when patron not found")
    void checkout_patronNotFound() {
        when(patronRepository.findById("bad")).thenReturn(Optional.empty());

        CheckoutRequest request = new CheckoutRequest();
        request.setPatronId("bad");
        request.setIsbn(ISBN);
        request.setBranchId(BRANCH_ID);

        assertThatThrownBy(() -> lendingService.checkoutBook(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
