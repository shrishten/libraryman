package com.library.service;

import com.library.dto.request.ReservationRequest;
import com.library.enums.BookStatus;
import com.library.enums.ReservationStatus;
import com.library.exception.BusinessException;
import com.library.model.Book;
import com.library.model.Patron;
import com.library.model.Reservation;
import com.library.observer.LibraryEventPublisher;
import com.library.repository.BookRepository;
import com.library.repository.PatronRepository;
import com.library.repository.ReservationRepository;
import com.library.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    private ReservationRepository reservationRepository;
    private BookRepository        bookRepository;
    private PatronRepository      patronRepository;
    private LibraryEventPublisher eventPublisher;
    private ReservationService    reservationService;

    private static final String PATRON_ID = UUID.randomUUID().toString();
    private static final String BRANCH_ID = UUID.randomUUID().toString();
    private static final String ISBN      = "978-0-06-112008-4";

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        bookRepository        = mock(BookRepository.class);
        patronRepository      = mock(PatronRepository.class);
        eventPublisher        = mock(LibraryEventPublisher.class);

        reservationService = new ReservationServiceImpl(
                reservationRepository, bookRepository, patronRepository, eventPublisher);
    }

    @Test
    @DisplayName("placeReservation: creates PENDING reservation when all copies are borrowed")
    void placeReservation_success() {
        Patron patron = Patron.builder().patronId(PATRON_ID).name("Carol").email("c@test.com")
                .memberSince(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        Book borrowed = Book.builder().bookId("b1").isbn(ISBN).title("T").author("A")
                .publicationYear(2000).branchId(BRANCH_ID).status(BookStatus.BORROWED)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(patronRepository.findById(PATRON_ID)).thenReturn(Optional.of(patron));
        when(bookRepository.findByIsbn(ISBN)).thenReturn(List.of(borrowed));
        when(reservationRepository.existsByPatronIdAndIsbnAndStatus(PATRON_ID, ISBN, ReservationStatus.PENDING))
                .thenReturn(false);
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(patronRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservationRequest request = new ReservationRequest();
        request.setPatronId(PATRON_ID); request.setIsbn(ISBN); request.setBranchId(BRANCH_ID);

        Reservation result = reservationService.placeReservation(request);

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(result.getIsbn()).isEqualTo(ISBN);
    }

    @Test
    @DisplayName("placeReservation: throws BusinessException when an available copy exists")
    void placeReservation_copyAvailable_throws() {
        Patron patron = Patron.builder().patronId(PATRON_ID).name("Carol").email("c@test.com")
                .memberSince(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        Book available = Book.builder().bookId("b1").isbn(ISBN).title("T").author("A")
                .publicationYear(2000).branchId(BRANCH_ID).status(BookStatus.AVAILABLE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(patronRepository.findById(PATRON_ID)).thenReturn(Optional.of(patron));
        when(bookRepository.findByIsbn(ISBN)).thenReturn(List.of(available));

        ReservationRequest request = new ReservationRequest();
        request.setPatronId(PATRON_ID); request.setIsbn(ISBN); request.setBranchId(BRANCH_ID);

        assertThatThrownBy(() -> reservationService.placeReservation(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("currently available");
    }

    @Test
    @DisplayName("cancelReservation: cancels a PENDING reservation successfully")
    void cancelReservation_success() {
        Reservation reservation = Reservation.builder()
                .reservationId("r1").isbn(ISBN).patronId(PATRON_ID).branchId(BRANCH_ID)
                .status(ReservationStatus.PENDING).reservedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).build();

        when(reservationRepository.findById("r1")).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Reservation result = reservationService.cancelReservation("r1", PATRON_ID);

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }
}
