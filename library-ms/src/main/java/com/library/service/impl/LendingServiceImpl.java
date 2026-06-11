package com.library.service.impl;

import com.library.dto.request.CheckoutRequest;
import com.library.enums.BookStatus;
import com.library.enums.LoanStatus;
import com.library.enums.ReservationStatus;
import com.library.exception.BusinessException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Patron;
import com.library.observer.LibraryEvent;
import com.library.observer.LibraryEventPublisher;
import com.library.repository.*;
import com.library.service.LendingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LendingServiceImpl implements LendingService {

    private static final Logger log = LoggerFactory.getLogger(LendingServiceImpl.class);
    private static final int LOAN_PERIOD_DAYS = 14;

    private final BookRepository bookRepository;
    private final PatronRepository patronRepository;
    private final LoanRepository loanRepository;
    private final BranchRepository branchRepository;
    private final ReservationRepository reservationRepository;
    private final LibraryEventPublisher eventPublisher;

    public LendingServiceImpl(BookRepository bookRepository,
                               PatronRepository patronRepository,
                               LoanRepository loanRepository,
                               BranchRepository branchRepository,
                               ReservationRepository reservationRepository,
                               LibraryEventPublisher eventPublisher) {
        this.bookRepository = bookRepository;
        this.patronRepository = patronRepository;
        this.loanRepository = loanRepository;
        this.branchRepository = branchRepository;
        this.reservationRepository = reservationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Loan checkoutBook(CheckoutRequest request) {
        Patron patron = patronRepository.findById(request.getPatronId())
                .orElseThrow(() -> new ResourceNotFoundException("Patron not found: " + request.getPatronId()));

        if (!branchRepository.existsById(request.getBranchId())) {
            throw new ResourceNotFoundException("Branch not found: " + request.getBranchId());
        }

        // Find an available copy of this ISBN at the given branch
        Book book = bookRepository.findByIsbn(request.getIsbn()).stream()
                .filter(b -> b.getBranchId().equals(request.getBranchId()) && b.isAvailable())
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "No available copy of ISBN '" + request.getIsbn()
                        + "' at branch '" + request.getBranchId() + "'."));

        // Mark book as borrowed
        book.setStatus(BookStatus.BORROWED);
        book.setUpdatedAt(LocalDateTime.now());
        bookRepository.save(book);

        // Create the loan record
        Loan loan = Loan.builder()
                .loanId(UUID.randomUUID().toString())
                .bookId(book.getBookId())
                .patronId(patron.getPatronId())
                .branchId(request.getBranchId())
                .checkoutDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(LOAN_PERIOD_DAYS))
                .status(LoanStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        loanRepository.save(loan);

        // Update patron's active loans list
        patron.getActiveLoans().add(loan.getLoanId());
        patronRepository.save(patron);

        // Fulfil any NOTIFIED reservation for this patron + isbn
        reservationRepository.findByIsbnAndStatus(request.getIsbn(), ReservationStatus.NOTIFIED).stream()
                .filter(r -> r.getPatronId().equals(patron.getPatronId()))
                .findFirst()
                .ifPresent(r -> {
                    r.markFulfilled();
                    reservationRepository.save(r);
                });

        eventPublisher.publish(LibraryEvent.builder()
                .type(LibraryEvent.Type.BOOK_CHECKED_OUT)
                .bookId(book.getBookId())
                .isbn(book.getIsbn())
                .patronId(patron.getPatronId())
                .branchId(request.getBranchId())
                .occurredAt(LocalDateTime.now())
                .build());

        log.info("Book checked out: loanId={}, bookId={}, patronId={}",
                loan.getLoanId(), book.getBookId(), patron.getPatronId());
        return loan;
    }

    @Override
    public Loan returnBook(String loanId) {
        Loan loan = getLoanById(loanId);

        if (!LoanStatus.ACTIVE.equals(loan.getStatus())) {
            throw new BusinessException("Loan '" + loanId + "' is not active.");
        }

        // Mark loan as returned
        loan.markReturned();
        loanRepository.save(loan);

        // Free up the book
        Book book = bookRepository.findById(loan.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + loan.getBookId()));
        book.setStatus(BookStatus.AVAILABLE);
        book.setUpdatedAt(LocalDateTime.now());
        bookRepository.save(book);

        // Update patron history
        patronRepository.findById(loan.getPatronId()).ifPresent(patron -> {
            patron.getActiveLoans().remove(loan.getLoanId());
            patron.addToBorrowingHistory(loan.getBookId());
            patronRepository.save(patron);
        });

        // Publish event — triggers ReservationNotificationListener
        eventPublisher.publish(LibraryEvent.builder()
                .type(LibraryEvent.Type.BOOK_RETURNED)
                .bookId(book.getBookId())
                .isbn(book.getIsbn())
                .patronId(loan.getPatronId())
                .branchId(loan.getBranchId())
                .occurredAt(LocalDateTime.now())
                .build());

        log.info("Book returned: loanId={}, bookId={}", loanId, book.getBookId());
        return loan;
    }

    @Override
    public Loan getLoanById(String loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));
    }
}
