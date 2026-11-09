package com.scholarmate.search.dto;

import java.util.List;

public record HybridSearchResponseDto(
    String query,
    int totalMatches,
    List<HybridSearchResultDto> results,
    String searchStrategy
) {}
