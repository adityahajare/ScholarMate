package com.scholarmate.discovery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryProgressDto {
    private Long sessionId;
    private String status;
    private int totalCandidatesCount;
    private int potentiallyEligibleCount;
    private int needsMoreInfoCount;
    private int notEligibleCount;
    private int pendingVerificationCount;
    private int questionsAnsweredCount;
    private int remainingQuestionsCount;
    private double progressPercentage;
    @Builder.Default
    private List<String> answeredFieldKeys = new ArrayList<>();
    @Builder.Default
    private List<String> remainingFieldKeys = new ArrayList<>();
}
