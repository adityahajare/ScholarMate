package com.scholarmate.controller;

import com.scholarmate.dto.StudentRecommendations;
import com.scholarmate.service.StudentRecommendationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/ml/recommendations")
public class MlRecommendationController {

    private final StudentRecommendationService recommendationService;
    private final com.scholarmate.service.MlRecommendationClient mlClient;

    public MlRecommendationController(StudentRecommendationService recommendationService,
                                      com.scholarmate.service.MlRecommendationClient mlClient) {
        this.recommendationService = recommendationService;
        this.mlClient = mlClient;
    }

    @GetMapping("/me")
    public StudentRecommendations recommendForCurrentStudent(
            @AuthenticationPrincipal UserDetails userDetails) {
        return recommendationService.recommendForStudent(userDetails.getUsername());
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        boolean healthy = mlClient.isHealthy();
        return Map.of("available", healthy, "service", "scholarmate-ml", "basePath", "/api/v1/recommend");
    }
}
