package com.scholarmate.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String title,
        String message,
        String type,
        String linkUrl,
        boolean isRead,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {}
