package com.scholarmate.service;

import com.scholarmate.dto.CreateNotificationRequest;
import com.scholarmate.dto.NotificationResponse;
import com.scholarmate.dto.NotificationSummaryResponse;
import com.scholarmate.entity.Notification;
import com.scholarmate.entity.User;
import com.scholarmate.repository.NotificationRepository;
import com.scholarmate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User testUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(101L);
        testUser.setEmail("student@scholarmate.in");
        testUser.setName("Test Student");
        testUser.setRole("ROLE_STUDENT");

        testNotification = Notification.builder()
                .id(1L)
                .user(testUser)
                .title("New Scholarship Matched")
                .message("You are eligible for Post Matric Scholarship.")
                .type("ELIGIBILITY")
                .linkUrl("/scholarships/1")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getNotificationsForUser returns paged DTOs")
    void testGetNotificationsForUser() {
        when(userRepository.findByEmail("student@scholarmate.in")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(101L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testNotification)));

        Page<NotificationResponse> result = notificationService.getNotificationsForUser(
                "student@scholarmate.in", false, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("New Scholarship Matched", result.getContent().get(0).title());
        assertFalse(result.getContent().get(0).isRead());
    }

    @Test
    @DisplayName("getSummaryForUser returns unread count and latest notifications")
    void testGetSummaryForUser() {
        when(userRepository.findByEmail("student@scholarmate.in")).thenReturn(Optional.of(testUser));
        when(notificationRepository.countByUserIdAndIsReadFalse(101L)).thenReturn(3L);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(101L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testNotification)));

        NotificationSummaryResponse summary = notificationService.getSummaryForUser("student@scholarmate.in");

        assertNotNull(summary);
        assertEquals(3L, summary.unreadCount());
        assertEquals(1, summary.notifications().size());
        assertEquals("ELIGIBILITY", summary.notifications().get(0).type());
    }

    @Test
    @DisplayName("markAsRead succeeds for notification owner")
    void testMarkAsReadSuccess() {
        when(userRepository.findByEmail("student@scholarmate.in")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.markAsRead("student@scholarmate.in", 1L);

        assertNotNull(response);
        assertTrue(response.isRead());
        assertNotNull(response.readAt());
    }

    @Test
    @DisplayName("markAsRead throws AccessDeniedException when another user attempts access")
    void testMarkAsReadUnauthorized() {
        User otherUser = new User();
        otherUser.setId(999L);
        otherUser.setEmail("intruder@scholarmate.in");

        when(userRepository.findByEmail("intruder@scholarmate.in")).thenReturn(Optional.of(otherUser));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

        assertThrows(AccessDeniedException.class, () ->
                notificationService.markAsRead("intruder@scholarmate.in", 1L)
        );
    }

    @Test
    @DisplayName("markAllAsRead delegates to repository batch update")
    void testMarkAllAsRead() {
        when(userRepository.findByEmail("student@scholarmate.in")).thenReturn(Optional.of(testUser));
        when(notificationRepository.markAllAsReadForUser(eq(101L), any(LocalDateTime.class))).thenReturn(5);

        int updated = notificationService.markAllAsRead("student@scholarmate.in");

        assertEquals(5, updated);
        verify(notificationRepository).markAllAsReadForUser(eq(101L), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("createNotification allows self creation")
    void testCreateNotificationSelf() {
        when(userRepository.findByEmail("student@scholarmate.in")).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId(200L);
            return n;
        });

        CreateNotificationRequest req = new CreateNotificationRequest(
                "Document Expiring",
                "Your income certificate expires soon.",
                "DOCUMENT",
                "/profile",
                null
        );

        NotificationResponse response = notificationService.createNotification("student@scholarmate.in", req);

        assertNotNull(response);
        assertEquals(200L, response.id());
        assertEquals("Document Expiring", response.title());
        assertEquals("DOCUMENT", response.type());
    }

    @Test
    @DisplayName("deleteNotification removes entity when owner requests")
    void testDeleteNotification() {
        when(userRepository.findByEmail("student@scholarmate.in")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

        notificationService.deleteNotification("student@scholarmate.in", 1L);

        verify(notificationRepository).delete(testNotification);
    }
}
