package com.scholarmate.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record StudentRecommendations(
        List<Recommendation> recommendations,
        int eligibleCandidateCount,
        MlRankingResponse.Pipeline pipeline) {

    public record Recommendation(
            Long scholarshipId,
            String name,
            String provider,
            String description,
            String applicationUrl,
            LocalDate applicationDeadline,
            double score,
            Map<String, Double> signals,
            Map<String, Integer> clusters,
            List<String> reasons) {
    }
}