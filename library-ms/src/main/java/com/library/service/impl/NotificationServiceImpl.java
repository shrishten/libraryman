package com.library.service.impl;

import com.library.exception.ResourceNotFoundException;
import com.library.model.Notification;
import com.library.repository.NotificationRepository;
import com.library.repository.PatronRepository;
import com.library.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final PatronRepository patronRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   PatronRepository patronRepository) {
        this.notificationRepository = notificationRepository;
        this.patronRepository = patronRepository;
    }

    @Override
    public List<Notification> getNotificationsForPatron(String patronId) {
        validatePatron(patronId);
        return notificationRepository.findByPatronId(patronId);
    }

    @Override
    public List<Notification> getUnreadNotificationsForPatron(String patronId) {
        validatePatron(patronId);
        return notificationRepository.findUnreadByPatronId(patronId);
    }

    @Override
    public Notification markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        notification.markRead();
        notificationRepository.save(notification);
        log.info("Notification marked as read: id={}", notificationId);
        return notification;
    }

    private void validatePatron(String patronId) {
        patronRepository.findById(patronId)
                .orElseThrow(() -> new ResourceNotFoundException("Patron not found: " + patronId));
    }
}
