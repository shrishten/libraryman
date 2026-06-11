package com.library.controller;

import com.library.model.Book;
import com.library.service.NotificationService;
import com.library.service.RecommendationService;
import com.library.model.Notification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for book recommendations and patron notifications.
 *
 * Base path: /api
 */
@RestController
@RequestMapping("/api")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final NotificationService notificationService;

    public RecommendationController(RecommendationService recommendationService,
                                    NotificationService notificationService) {
        this.recommendationService = recommendationService;
        this.notificationService = notificationService;
    }

    /**
     * Get personalised book recommendations for a patron.
     *
     * @param patronId the patron to recommend books for
     * @param strategy one of: GENRE_BASED, AUTHOR_BASED, POPULARITY_BASED (default: GENRE_BASED)
     * @param limit    max results to return (default: 5)
     */
    @GetMapping("/patrons/{patronId}/recommendations")
    public ResponseEntity<List<Book>> getRecommendations(
            @PathVariable String patronId,
            @RequestParam(defaultValue = "GENRE_BASED") String strategy,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(recommendationService.getRecommendations(patronId, strategy, limit));
    }

    /** List all available recommendation strategy names. */
    @GetMapping("/recommendations/strategies")
    public ResponseEntity<List<String>> getStrategies() {
        return ResponseEntity.ok(recommendationService.getAvailableStrategies());
    }

    /** Get all notifications for a patron. */
    @GetMapping("/patrons/{patronId}/notifications")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable String patronId) {
        return ResponseEntity.ok(notificationService.getNotificationsForPatron(patronId));
    }

    /** Get only unread notifications for a patron. */
    @GetMapping("/patrons/{patronId}/notifications/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable String patronId) {
        return ResponseEntity.ok(notificationService.getUnreadNotificationsForPatron(patronId));
    }

    /** Mark a notification as read. */
    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable String notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }
}
