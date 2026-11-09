package com.scholarmate.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record FeedbackRequestDto(
    @NotNull Long scholarshipId,
    @NotBlank String eventType,
    String recommendationVersion,
    String modelVersion,
    Map<String, Object> contextData
) {}
