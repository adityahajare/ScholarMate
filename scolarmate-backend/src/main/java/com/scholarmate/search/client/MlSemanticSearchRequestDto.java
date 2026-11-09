package com.scholarmate.search.client;

import java.util.List;

public record MlSemanticSearchRequestDto(
    String query,
    List<MlDocSnippet> documents,
    int topK
) {
    public record MlDocSnippet(
        Long id,
        String name,
        String description,
        String provider,
        String category
    ) {}
}
