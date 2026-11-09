package com.scholarmate.feedback.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class MlFeedbackClient {

    private static final Logger log = LoggerFactory.getLogger(MlFeedbackClient.class);

    private final RestClient restClient;

    public MlFeedbackClient(@Value("${scholarmate.ml.base-url:http://localhost:8000}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public MlFeedbackResponseDto sendFeedback(MlFeedbackEventDto event) {
        try {
            return restClient.post()
                    .uri("/api/v1/online-learning/feedback")
                    .body(event)
                    .retrieve()
                    .body(MlFeedbackResponseDto.class);
        } catch (Exception e) {
            log.warn("Failed to send online feedback to ML service (student={}, scholarship={}, event={}): {}",
                    event.studentId(), event.scholarshipId(), event.eventType(), e.getMessage());
            return new MlFeedbackResponseDto("failed", event.eventType(), 0, 0.5, "river-online-v1", e.getMessage());
        }
    }

    public boolean sendBatch(List<MlFeedbackEventDto> events) {
        if (events == null || events.isEmpty()) {
            return true;
        }
        try {
            restClient.post()
                    .uri("/api/v1/online-learning/train-batch")
                    .body(Map.of("events", events))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.warn("Failed to send batch feedback to ML service (size={}): {}", events.size(), e.getMessage());
            return false;
        }
    }
}
