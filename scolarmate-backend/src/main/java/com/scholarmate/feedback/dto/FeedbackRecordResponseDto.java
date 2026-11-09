package com.scholarmate.feedback.dto;

import java.time.Instant;
import java.util.Map;

public record FeedbackRecordResponseDto(
    Long id,
    Long studentId,
    Long scholarshipId,
    String scholarshipName,
    String eventType,
    Instant eventTimestamp,
    String recommendationVersion,
    String modelVersion,
    boolean processedForTraining,
    Map<String, Object> contextData
) {}
