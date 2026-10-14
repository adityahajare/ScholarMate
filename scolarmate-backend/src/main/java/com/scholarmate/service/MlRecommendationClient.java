package com.scholarmate.service;

import com.scholarmate.dto.MlRankingResponse;
import com.scholarmate.dto.MlRecommendationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MlRecommendationClient {

    private static final Logger log = LoggerFactory.getLogger(MlRecommendationClient.class);

    private final RestClient restClient;

    public MlRecommendationClient(
            @Value("${scholarmate.ml.base-url:http://localhost:8000}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public MlRankingResponse rank(MlRecommendationRequest request) {
        long startedAt = System.nanoTime();
        try {
            MlRankingResponse response = restClient.post()
                    .uri("/api/v1/recommend")
                    .body(request)
                    .retrieve()
                    .body(MlRankingResponse.class);
            log.info("ML recommendation completed durationMs={}", elapsedMillis(startedAt));
            return response;
        } catch (RuntimeException e) {
            log.warn("ML recommendation failed durationMs={} error={}", elapsedMillis(startedAt), e.getMessage());
            throw e;
        }
    }

    public boolean isHealthy() {
        try {
            restClient.get().uri("/health").retrieve().toBodilessEntity();
            return true;
        } catch (RuntimeException exception) {
            log.debug("ML health check failed: {}", exception.getMessage());
            return false;
        }
    }

    private static long elapsedMillis(long startedAt) {
        return java.time.Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }
}
