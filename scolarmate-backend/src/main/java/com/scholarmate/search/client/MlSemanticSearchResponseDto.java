package com.scholarmate.search.client;

import java.util.List;

public record MlSemanticSearchResponseDto(
    List<MlSemanticMatch> results,
    String query,
    String model
) {
    public record MlSemanticMatch(
        Long scholarshipId,
        double score
    ) {}
}
