package com.scholarmate.service;

import com.scholarmate.dto.CreateNotificationRequest;
import com.scholarmate.dto.NotificationResponse;
import com.scholarmate.dto.NotificationSummaryResponse;
import com.scholarmate.entity.Notification;
import com.scholarmate.entity.User;
import com.scholarmate.repository.NotificationRepository;
import com.scholarmate.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsForUser(String email, Boolean unreadOnly, Pageable pageable) {
        User user = findUserByEmail(email);
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Notification> page;
        if (Boolean.TRUE.equals(unreadOnly)) {
            page = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId(), sortedPageable);
        } else {
            page = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), sortedPageable);
        }
        return page.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public NotificationSummaryResponse getSummaryForUser(String email) {
        User user = findUserByEmail(email);
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
        Pageable topTen = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<NotificationResponse> latest = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), topTen)
                .map(this::toDto)
                .getContent();

        return new NotificationSummaryResponse(unreadCount, latest);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        User user = findUserByEmail(email);
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public NotificationResponse markAsRead(String email, Long notificationId) {
        User user = findUserByEmail(email);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + notificationId));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to update this notification.");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return toDto(notification);
    }

    @Transactional
    public int markAllAsRead(String email) {
        User user = findUserByEmail(email);
        return notificationRepository.markAllAsReadForUser(user.getId(), LocalDateTime.now());
    }

    @Transactional
    public void deleteNotification(String email, Long notificationId) {
        User user = findUserByEmail(email);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + notificationId));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this notification.");
        }

        notificationRepository.delete(notification);
    }

    @Transactional
    public NotificationResponse createNotification(String callerEmail, CreateNotificationRequest request) {
        User caller = findUserByEmail(callerEmail);
        User targetUser;

        if (request.userId() != null) {
            boolean isAdmin = "ADMIN".equalsIgnoreCase(caller.getRole()) || "ROLE_ADMIN".equalsIgnoreCase(caller.getRole());
            if (!caller.getId().equals(request.userId()) && !isAdmin) {
                throw new AccessDeniedException("Only admins can create notifications for other users.");
            }
            targetUser = userRepository.findById(request.userId())
                    .orElseThrow(() -> new IllegalArgumentException("Target user not found with id: " + request.userId()));
        } else {
            targetUser = caller;
        }

        Notification notification = Notification.builder()
                .user(targetUser)
                .title(request.title().trim())
                .message(request.message().trim())
                .type(request.type() != null && !request.type().isBlank() ? request.type().trim().toUpperCase() : "SYSTEM")
                .linkUrl(request.linkUrl() != null ? request.linkUrl().trim() : null)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        return toDto(saved);
    }

    @Transactional
    public NotificationResponse createSystemNotification(Long userId, String title, String message, String type, String linkUrl) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Notification notification = Notification.builder()
                .user(targetUser)
                .title(title)
                .message(message)
                .type(type != null ? type : "SYSTEM")
                .linkUrl(linkUrl)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        return toDto(notificationRepository.save(notification));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found for email: " + email));
    }

    private NotificationResponse toDto(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.getLinkUrl(),
                n.isRead(),
                n.getReadAt(),
                n.getCreatedAt()
        );
    }
}
