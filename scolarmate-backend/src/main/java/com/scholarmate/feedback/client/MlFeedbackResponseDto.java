package com.scholarmate.feedback.client;

public record MlFeedbackResponseDto(
    String status,
    String eventType,
    long samplesSeen,
    double onlineAuc,
    String modelVersion,
    String message
) {}
