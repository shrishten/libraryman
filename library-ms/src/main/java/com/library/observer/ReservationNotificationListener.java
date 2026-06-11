package com.library.observer;

import com.library.enums.NotificationType;
import com.library.enums.ReservationStatus;
import com.library.model.Notification;
import com.library.model.Reservation;
import com.library.repository.NotificationRepository;
import com.library.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Observer that listens for BOOK_RETURNED events and notifies patrons
 * who have a pending reservation for that ISBN.
 *
 * Design Pattern: Observer (Concrete Listener role)
 */
@Component
public class ReservationNotificationListener implements LibraryEventListener {

    private static final Logger log = LoggerFactory.getLogger(ReservationNotificationListener.class);

    private final ReservationRepository reservationRepository;
    private final NotificationRepository notificationRepository;

    public ReservationNotificationListener(ReservationRepository reservationRepository,
                                           NotificationRepository notificationRepository,
                                           LibraryEventPublisher publisher) {
        this.reservationRepository = reservationRepository;
        this.notificationRepository = notificationRepository;
        publisher.subscribe(this);
    }

    @Override
    public void onEvent(LibraryEvent event) {
        if (event.getIsbn() == null) return;

        List<Reservation> pending = reservationRepository
                .findByIsbnAndStatus(event.getIsbn(), ReservationStatus.PENDING);

        for (Reservation reservation : pending) {
            reservation.markNotified();
            reservationRepository.save(reservation);

            Notification notification = Notification.builder()
                    .notificationId(UUID.randomUUID().toString())
                    .patronId(reservation.getPatronId())
                    .type(NotificationType.RESERVATION_AVAILABLE)
                    .message(String.format(
                            "Good news! A copy of the book with ISBN '%s' is now available at branch '%s'. "
                          + "Please visit us within 3 days to check it out.",
                            event.getIsbn(), reservation.getBranchId()))
                    .read(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
            log.info("Reservation notification sent to patron={} for isbn={}",
                    reservation.getPatronId(), event.getIsbn());
        }
    }

    @Override
    public LibraryEvent.Type[] subscribedTypes() {
        return new LibraryEvent.Type[]{LibraryEvent.Type.BOOK_RETURNED};
    }
}
