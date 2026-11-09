package com.scholarmate.feedback.client;

import java.util.Map;

public record MlFeedbackEventDto(
    Long studentId,
    Long scholarshipId,
    String eventType,
    Map<String, Object> features,
    String eventTimestamp
) {}
