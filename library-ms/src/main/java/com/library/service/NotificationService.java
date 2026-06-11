package com.library.service;

import com.library.model.Notification;

import java.util.List;

public interface NotificationService {

    List<Notification> getNotificationsForPatron(String patronId);

    List<Notification> getUnreadNotificationsForPatron(String patronId);

    Notification markAsRead(String notificationId);
}
