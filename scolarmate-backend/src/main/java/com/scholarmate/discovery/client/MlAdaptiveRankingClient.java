package com.scholarmate.discovery.client;

import com.scholarmate.discovery.dto.AdaptiveQuestionRankingRequestDto;
import com.scholarmate.discovery.dto.AdaptiveQuestionRankingResponseDto;
import com.scholarmate.discovery.dto.AdaptiveQuestionRankingResponseDto.RankedQuestionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.*;

@Component
@Slf4j
public class MlAdaptiveRankingClient {

    private final RestClient restClient;
    private final boolean mlEnabled;

    private static final Map<String, Double> FALLBACK_WEIGHTS = Map.of(
            "annual_family_income", 0.40,
            "domicile_state", 0.35,
            "caste_category", 0.30,
            "education_level", 0.28,
            "course_name", 0.26,
            "previous_percentage", 0.25,
            "current_year", 0.22,
            "gender", 0.20,
            "disability_status", 0.18,
            "accommodation_type", 0.15
    );

    public MlAdaptiveRankingClient(
            @Value("${scholarmate.ml.base-url:http://localhost:8000}") String baseUrl,
            @Value("${scholarmate.ml.enabled:true}") boolean mlEnabled) {
        this.mlEnabled = mlEnabled;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public AdaptiveQuestionRankingResponseDto rankQuestions(AdaptiveQuestionRankingRequestDto request) {
        if (request.getMissingFieldKeys().isEmpty()) {
            return new AdaptiveQuestionRankingResponseDto();
        }

        if (mlEnabled) {
            long start = System.currentTimeMillis();
            try {
                AdaptiveQuestionRankingResponseDto response = restClient.post()
                        .uri("/api/v1/adaptive/rank-questions")
                        .body(request)
                        .retrieve()
                        .body(AdaptiveQuestionRankingResponseDto.class);

                if (response != null && response.getRankedQuestions() != null) {
                    log.info("ML adaptive question ranking returned {} questions in {}ms",
                            response.getRankedQuestions().size(), System.currentTimeMillis() - start);
                    return response;
                }
            } catch (Exception e) {
                log.warn("FastAPI ML service ranking call failed ({}). Using deterministic fallback ranking.", e.getMessage());
            }
        }

        // Deterministic local ranking fallback
        return performDeterministicRanking(request);
    }

    private AdaptiveQuestionRankingResponseDto performDeterministicRanking(AdaptiveQuestionRankingRequestDto request) {
        List<String> missing = request.getMissingFieldKeys().stream()
                .filter(k -> !request.getAlreadyAskedFieldKeys().contains(k))
                .toList();

        Map<String, List<String>> reqMap = request.getScholarshipRequirementFieldMap();
        int totalCandidates = Math.max(request.getCandidateScholarshipIds().size(), 1);

        List<RankedQuestionDto> rankedList = new ArrayList<>();

        for (String fieldKey : missing) {
            List<String> targetSchs = reqMap.getOrDefault(fieldKey, Collections.emptyList());
            int targetCount = targetSchs.size();
            double coverage = (double) targetCount / totalCandidates;
            double discWeight = FALLBACK_WEIGHTS.getOrDefault(fieldKey, 0.10);

            double score = (targetCount > 0)
                    ? Math.min(0.99, Math.max(0.10, (coverage * 0.60) + (discWeight * 0.40)))
                    : Math.min(0.30, Math.max(0.05, discWeight * 0.50));

            String reason = generateFallbackReason(fieldKey, targetCount);

            rankedList.add(RankedQuestionDto.builder()
                    .fieldKey(fieldKey)
                    .priorityScore(Math.round(score * 1000.0) / 1000.0)
                    .reason(reason)
                    .targetScholarshipCount(targetCount)
                    .build());
        }

        rankedList.sort(Comparator
                .comparingDouble(RankedQuestionDto::getPriorityScore)
                .thenComparingInt(RankedQuestionDto::getTargetScholarshipCount)
                .reversed());

        return AdaptiveQuestionRankingResponseDto.builder()
                .rankedQuestions(rankedList)
                .build();
    }

    private String generateFallbackReason(String fieldKey, int count) {
        if (count == 0) {
            return "Helps refine your academic profile for broader scholarship opportunities";
        }
        return switch (fieldKey) {
            case "annual_family_income" -> "Required to verify income ceiling eligibility across " + count + " candidate scholarship(s)";
            case "domicile_state", "permanent_state" -> "Resolves state domicile eligibility for " + count + " candidate scheme(s)";
            case "caste_category" -> "Checks category reservation and concession criteria for " + count + " scheme(s)";
            case "previous_percentage", "cgpa" -> "Evaluates merit percentage thresholds for " + count + " scholarship(s)";
            case "accommodation_type" -> "Checks hosteller allowance qualification for " + count + " scheme(s)";
            default -> "Directly required by verified rules of " + count + " candidate scholarship(s)";
        };
    }
}
