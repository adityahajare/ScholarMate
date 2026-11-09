package com.scholarmate.search.dto;

import java.util.List;

public record HybridSearchResultDto(
    Long scholarshipId,
    String name,
    String provider,
    String category,
    Double amount,
    String applicationDeadline,
    String applicationUrl,
    double fuzzyScore,
    double semanticScore,
    double finalScore,
    String eligibilityStatus,
    List<String> matchReasons
) {}
