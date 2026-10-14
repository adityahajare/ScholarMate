package com.scholarmate.dto;

import java.time.LocalDateTime;

public record DocumentReviewHistoryItem(
        Long id,
        String inputMethod,
        LocalDateTime createdAt,
        String responseJson) {
}
