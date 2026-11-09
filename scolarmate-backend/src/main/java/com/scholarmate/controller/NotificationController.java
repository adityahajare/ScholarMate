package com.scholarmate.controller;

import com.scholarmate.dto.CreateNotificationRequest;
import com.scholarmate.dto.NotificationResponse;
import com.scholarmate.dto.NotificationSummaryResponse;
import com.scholarmate.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean unreadOnly) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Page<NotificationResponse> result = notificationService.getNotificationsForUser(
                authentication.getName(),
                unreadOnly,
                PageRequest.of(safePage, safeSize)
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/summary")
    public ResponseEntity<NotificationSummaryResponse> getSummary(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getSummaryForUser(authentication.getName()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        long count = notificationService.getUnreadCount(authentication.getName());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsReadPatch(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(authentication.getName(), id));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsReadPost(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(authentication.getName(), id));
    }

    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        int updated = notificationService.markAllAsRead(authentication.getName());
        return ResponseEntity.ok(Map.of("updatedCount", updated, "message", "All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(
            Authentication authentication,
            @PathVariable Long id) {
        notificationService.deleteNotification(authentication.getName(), id);
        return ResponseEntity.ok(Map.of("message", "Notification deleted successfully"));
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            Authentication authentication,
            @Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse created = notificationService.createNotification(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
