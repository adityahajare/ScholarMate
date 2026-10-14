package com.scholarmate.service;

import com.scholarmate.dto.MlRankingResponse;
import com.scholarmate.dto.MlRecommendationRequest;
import com.scholarmate.dto.StudentRecommendations;
import com.scholarmate.entity.EligibilityCriteria;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.User;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.repository.EligibilityCriteriaRepository;
import com.scholarmate.repository.ScholarshipRepository;
import com.scholarmate.repository.UserProfileRepository;
import com.scholarmate.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StudentRecommendationService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final EligibilityCriteriaRepository criteriaRepository;
    private final EligibilityEvaluationService eligibilityEvaluator;
    private final MlRecommendationClient mlClient;

    public StudentRecommendationService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            ScholarshipRepository scholarshipRepository,
            EligibilityCriteriaRepository criteriaRepository,
            EligibilityEvaluationService eligibilityEvaluator,
            MlRecommendationClient mlClient) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.scholarshipRepository = scholarshipRepository;
        this.criteriaRepository = criteriaRepository;
        this.eligibilityEvaluator = eligibilityEvaluator;
        this.mlClient = mlClient;
    }

    public StudentRecommendations recommendForStudent(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student profile not found"));
        List<Scholarship> activeScholarships = scholarshipRepository.findByActiveTrue();
        if (activeScholarships.isEmpty()) {
            return new StudentRecommendations(List.of(), 0, null);
        }

        List<Long> scholarshipIds = activeScholarships.stream()
                .map(scholarship -> scholarship.getId())
                .filter(java.util.Objects::nonNull)
                .toList();
        Map<Long, List<EligibilityCriteria>> criteriaByScholarship = criteriaRepository
                .findByScholarshipIdIn(scholarshipIds)
                .stream()
                .collect(Collectors.groupingBy(criteria -> criteria.getScholarship().getId()));

        List<Scholarship> eligibleScholarships = activeScholarships.stream()
                .filter(scholarship -> "ELIGIBLE".equalsIgnoreCase(
                        eligibilityEvaluator.evaluate(
                                profile,
                                scholarship,
                                criteriaByScholarship.getOrDefault(scholarship.getId(), List.of())
                        ).getStatus()))
                .toList();
        if (eligibleScholarships.isEmpty()) {
            return new StudentRecommendations(List.of(), 0, null);
        }

        Set<Long> eligibleIds = eligibleScholarships.stream()
                .map(scholarship -> scholarship.getId())
                .collect(Collectors.toSet());
        MlRecommendationRequest request = new MlRecommendationRequest(
                new MlRecommendationRequest.Student(
                        profile.getCourse(),
                        profile.getCollege(),
                        profile.getCgpa(),
                        profile.getFamilyIncome(),
                        profile.getCategory(),
                        profile.getState(),
                        profile.getDomicileState(),
                        profile.getYearOfStudy(),
                        profile.getGender(),
                        profile.getEducationStage(),
                        profile.getFeatures() == null ? Map.of() : profile.getFeatures(),
                        profile.getBehaviors() == null ? Map.of() : profile.getBehaviors()),
                eligibleScholarships.stream().map(this::toCandidate).toList());

        MlRankingResponse ranking;
        try {
            ranking = mlClient.rank(request);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "ML recommendation service is unavailable",
                    exception);
        }
        if (ranking == null || ranking.recommendations() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "ML service returned an empty response");
        }

        Map<Long, Scholarship> scholarshipsById = eligibleScholarships.stream()
                .collect(Collectors.toMap(scholarship -> scholarship.getId(), Function.identity()));
        List<StudentRecommendations.Recommendation> recommendations = new ArrayList<>();
        Set<Long> returnedIds = new java.util.HashSet<>();
        for (MlRankingResponse.RankedScholarship ranked : ranking.recommendations()) {
            if (ranked == null || ranked.scholarshipId() == null
                    || !eligibleIds.contains(ranked.scholarshipId())
                    || !returnedIds.add(ranked.scholarshipId())) {
                continue;
            }
            Scholarship scholarship = scholarshipsById.get(ranked.scholarshipId());
            recommendations.add(new StudentRecommendations.Recommendation(
                    scholarship.getId(),
                    scholarship.getName(),
                    scholarship.getProvider(),
                    scholarship.getDescription(),
                    scholarship.getApplicationUrl(),
                    scholarship.getApplicationDeadline(),
                    ranked.score(),
                    ranked.signals(),
                    ranked.clusters(),
                    ranked.reasons()));
        }
        return new StudentRecommendations(recommendations, eligibleScholarships.size(), ranking.pipeline());
    }

    private MlRecommendationRequest.Candidate toCandidate(Scholarship scholarship) {
        return new MlRecommendationRequest.Candidate(
                scholarship.getId(),
                scholarship.getName(),
                scholarship.getDescription(),
                scholarship.getProvider(),
                scholarship.getCourse(),
                scholarship.getCategory(),
                scholarship.getEligibleCategories(),
                scholarship.getState(),
                scholarship.getMinimumCgpa(),
                scholarship.getMaximumFamilyIncome(),
                scholarship.getMinimumYearOfStudy(),
                scholarship.getMaximumYearOfStudy(),
                scholarship.getAmount(),
                scholarship.getApplicationDeadline(),
                scholarship.getApplicationUrl(),
                scholarship.getSourceUrl(),
                scholarship.getFeatures() == null ? Map.of() : scholarship.getFeatures());
    }
}
