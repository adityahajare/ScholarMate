package com.scholarmate.search.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Component
public class MlSemanticSearchClient {

    private static final Logger log = LoggerFactory.getLogger(MlSemanticSearchClient.class);

    private final RestClient restClient;

    public MlSemanticSearchClient(@Value("${scholarmate.ml.base-url:http://localhost:8000}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public List<MlSemanticSearchResponseDto.MlSemanticMatch> search(String query, List<MlSemanticSearchRequestDto.MlDocSnippet> docs, int topK) {
        if (query == null || query.isBlank() || docs == null || docs.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            MlSemanticSearchRequestDto request = new MlSemanticSearchRequestDto(query, docs, topK);
            MlSemanticSearchResponseDto response = restClient.post()
                    .uri("/api/v1/search/semantic")
                    .body(request)
                    .retrieve()
                    .body(MlSemanticSearchResponseDto.class);

            if (response != null && response.results() != null) {
                return response.results();
            }
        } catch (Exception e) {
            log.warn("ML semantic search call failed for query='{}': {}", query, e.getMessage());
        }
        return Collections.emptyList();
    }
}
