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
public class AdaptiveQuestionRankingResponseDto {
    @Builder.Default
    private List<RankedQuestionDto> rankedQuestions = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankedQuestionDto {
        private String fieldKey;
        private double priorityScore;
        private String reason;
        private int targetScholarshipCount;
    }
}
