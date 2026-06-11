package com.library.service.impl;

import com.library.dto.request.ReservationRequest;
import com.library.enums.BookStatus;
import com.library.enums.ReservationStatus;
import com.library.exception.BusinessException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Reservation;
import com.library.observer.LibraryEvent;
import com.library.observer.LibraryEventPublisher;
import com.library.repository.BookRepository;
import com.library.repository.PatronRepository;
import com.library.repository.ReservationRepository;
import com.library.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final PatronRepository patronRepository;
    private final LibraryEventPublisher eventPublisher;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                   BookRepository bookRepository,
                                   PatronRepository patronRepository,
                                   LibraryEventPublisher eventPublisher) {
        this.reservationRepository = reservationRepository;
        this.bookRepository = bookRepository;
        this.patronRepository = patronRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Reservation placeReservation(ReservationRequest request) {
        // Patron must exist
        patronRepository.findById(request.getPatronId())
                .orElseThrow(() -> new ResourceNotFoundException("Patron not found: " + request.getPatronId()));

        // No copies should be currently available (otherwise just check out)
        boolean hasAvailableCopy = bookRepository.findByIsbn(request.getIsbn()).stream()
                .anyMatch(b -> b.getBranchId().equals(request.getBranchId())
                        && BookStatus.AVAILABLE.equals(b.getStatus()));

        if (hasAvailableCopy) {
            throw new BusinessException("A copy of ISBN '" + request.getIsbn()
                    + "' is currently available at this branch. Please proceed to checkout instead.");
        }

        // Patron must not already have a pending reservation for this ISBN at this branch
        boolean alreadyReserved = reservationRepository
                .existsByPatronIdAndIsbnAndStatus(request.getPatronId(), request.getIsbn(), ReservationStatus.PENDING);

        if (alreadyReserved) {
            throw new BusinessException("Patron already has a pending reservation for ISBN '" + request.getIsbn() + "'.");
        }

        Reservation reservation = Reservation.builder()
                .reservationId(UUID.randomUUID().toString())
                .isbn(request.getIsbn())
                .patronId(request.getPatronId())
                .branchId(request.getBranchId())
                .status(ReservationStatus.PENDING)
                .reservedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        reservationRepository.save(reservation);

        patronRepository.findById(request.getPatronId()).ifPresent(p -> {
            p.getReservations().add(reservation.getReservationId());
            patronRepository.save(p);
        });

        eventPublisher.publish(LibraryEvent.builder()
                .type(LibraryEvent.Type.RESERVATION_PLACED)
                .isbn(request.getIsbn())
                .patronId(request.getPatronId())
                .branchId(request.getBranchId())
                .occurredAt(LocalDateTime.now())
                .build());

        log.info("Reservation placed: id={}, isbn={}, patronId={}",
                reservation.getReservationId(), request.getIsbn(), request.getPatronId());
        return reservation;
    }

    @Override
    public Reservation cancelReservation(String reservationId, String patronId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + reservationId));

        if (!reservation.getPatronId().equals(patronId)) {
            throw new BusinessException("Reservation does not belong to patron: " + patronId);
        }

        if (ReservationStatus.FULFILLED.equals(reservation.getStatus())
                || ReservationStatus.CANCELLED.equals(reservation.getStatus())) {
            throw new BusinessException("Reservation is already " + reservation.getStatus());
        }

        reservation.cancel();
        reservationRepository.save(reservation);
        log.info("Reservation cancelled: id={}", reservationId);
        return reservation;
    }

    @Override
    public List<Reservation> getReservationsByPatron(String patronId) {
        patronRepository.findById(patronId)
                .orElseThrow(() -> new ResourceNotFoundException("Patron not found: " + patronId));
        return reservationRepository.findByPatronId(patronId);
    }

    @Override
    public List<Reservation> getPendingReservationsByIsbn(String isbn) {
        return reservationRepository.findByIsbnAndStatus(isbn, ReservationStatus.PENDING);
    }
}
