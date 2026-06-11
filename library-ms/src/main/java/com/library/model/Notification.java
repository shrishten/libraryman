package com.library.model;

import com.library.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents a notification sent to a patron.
 */
@Data
@Builder
public class Notification {

    private String notificationId;
    private String patronId;
    private NotificationType type;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    /**
     * Marks this notification as read.
     */
    public void markRead() {
        this.read = true;
    }
}
