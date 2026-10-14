package com.scholarmate.controller;

import com.scholarmate.dto.EligibilityAssessment;
import com.scholarmate.entity.EligibilityCriteria;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.repository.EligibilityCriteriaRepository;
import com.scholarmate.repository.ScholarshipRepository;
import com.scholarmate.repository.UserProfileRepository;
import com.scholarmate.service.EligibilityEvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@RestController
@RequestMapping("/api/eligibility")
public class EligibilityEvaluationController {

    private final EligibilityEvaluationService eligibilityEvaluationService;
    private final UserProfileRepository userProfileRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final EligibilityCriteriaRepository eligibilityCriteriaRepository;

    public EligibilityEvaluationController(
            EligibilityEvaluationService eligibilityEvaluationService,
            UserProfileRepository userProfileRepository,
            ScholarshipRepository scholarshipRepository,
            EligibilityCriteriaRepository eligibilityCriteriaRepository) {
        this.eligibilityEvaluationService = eligibilityEvaluationService;
        this.userProfileRepository = userProfileRepository;
        this.scholarshipRepository = scholarshipRepository;
        this.eligibilityCriteriaRepository = eligibilityCriteriaRepository;
    }

    @GetMapping("/scholarships/{scholarshipId}/users/{userId}")
    public ResponseEntity<EligibilityAssessment> evaluate(
            @PathVariable Long scholarshipId,
            @PathVariable Long userId,
            Authentication authentication) {

        Long authenticatedUserId = userProfileRepository.findByUserEmail(authentication.getName())
                .map(profile -> profile.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("You can only evaluate your own eligibility.");
        }

        return evaluateForUser(scholarshipId, userId);
    }

    @GetMapping("/scholarships/{scholarshipId}/me")
    public ResponseEntity<EligibilityAssessment> evaluateMine(
            @PathVariable Long scholarshipId,
            Authentication authentication) {
        Long userId = userProfileRepository.findByUserEmail(authentication.getName())
                .map(profile -> profile.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return evaluateForUser(scholarshipId, userId);
    }

    private ResponseEntity<EligibilityAssessment> evaluateForUser(Long scholarshipId, Long userId) {

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        Scholarship scholarship = scholarshipRepository.findById(scholarshipId)
                .orElseThrow(() -> new RuntimeException("Scholarship not found"));

        List<EligibilityCriteria> criteria =
                eligibilityCriteriaRepository.findByScholarshipId(scholarshipId);

        EligibilityAssessment assessment =
                eligibilityEvaluationService.evaluate(profile, scholarship, criteria);

        return ResponseEntity.ok(assessment);
    }
}
