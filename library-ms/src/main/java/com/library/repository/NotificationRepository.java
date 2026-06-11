package com.library.repository;

import com.library.model.Notification;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory repository for Notification entities.
 */
@Repository
public class NotificationRepository {

    private final Map<String, Notification> store = new ConcurrentHashMap<>();

    public Notification save(Notification notification) {
        store.put(notification.getNotificationId(), notification);
        return notification;
    }

    public Optional<Notification> findById(String notificationId) {
        return Optional.ofNullable(store.get(notificationId));
    }

    public List<Notification> findByPatronId(String patronId) {
        return store.values().stream()
                .filter(n -> patronId.equals(n.getPatronId()))
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Notification> findUnreadByPatronId(String patronId) {
        return store.values().stream()
                .filter(n -> patronId.equals(n.getPatronId()) && !n.isRead())
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
}
