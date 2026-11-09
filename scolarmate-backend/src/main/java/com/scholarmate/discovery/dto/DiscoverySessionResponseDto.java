package com.scholarmate.discovery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoverySessionResponseDto {
    private Long sessionId;
    private String status; // IN_PROGRESS, COMPLETED, STOPPED
    private int candidateScholarshipCount;
    private int questionsAnsweredCount;
    private int remainingQuestionsCount;
    private boolean hasSufficientInformation;
    @Builder.Default
    private List<AdaptiveQuestionDto> nextQuestions = new ArrayList<>();
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
