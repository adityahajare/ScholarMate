package com.scholarmate.dto;

import java.util.List;
import java.util.Map;

public record MlRankingResponse(
        List<RankedScholarship> recommendations,
        Pipeline pipeline) {

    public record RankedScholarship(
            Long scholarshipId,
            double score,
            Map<String, Double> signals,
            Map<String, Integer> clusters,
            List<String> reasons) {
    }

    public record Pipeline(
            String version,
            int candidateCount,
            int latentDimensions,
            String semanticModel,
            String semanticStatus,
            Map<String, Map<String, Double>> clusteringMetrics,
            Map<String, Double> rankingWeights) {
    }
}