package com.scholarmate.service;

import com.scholarmate.dto.MlRankingResponse;
import com.scholarmate.dto.MlRecommendationRequest;
import com.scholarmate.dto.StudentRecommendations;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.User;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.repository.EligibilityCriteriaRepository;
import com.scholarmate.repository.ScholarshipRepository;
import com.scholarmate.repository.UserProfileRepository;
import com.scholarmate.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudentRecommendationServiceTest {

    @Test
    void sendsOnlyConfirmedEligibleScholarshipsToMlService() {
        UserRepository userRepository = mock(UserRepository.class);
        UserProfileRepository profileRepository = mock(UserProfileRepository.class);
        ScholarshipRepository scholarshipRepository = mock(ScholarshipRepository.class);
        EligibilityCriteriaRepository criteriaRepository = mock(EligibilityCriteriaRepository.class);
        MlRecommendationClient mlClient = mock(MlRecommendationClient.class);

        User user = new User();
        user.setId(10L);
        user.setEmail("student@example.com");
        UserProfile profile = UserProfile.builder()
                .course("B.Tech")
                .state("Maharashtra")
                .category("OBC")
                .cgpa(8.5)
                .familyIncome(250000.0)
                .yearOfStudy(3)
                .build();
        Scholarship eligible = Scholarship.builder()
                .id(101L)
                .name("Merit Scholarship")
                .course("B.Tech")
                .state("Maharashtra")
                .category("OBC")
                .minimumCgpa(7.0)
                .maximumFamilyIncome(800000.0)
                .minimumYearOfStudy(1)
                .maximumYearOfStudy(4)
                .build();
        Scholarship ineligible = Scholarship.builder()
                .id(102L)
                .name("Higher CGPA Scholarship")
                .minimumCgpa(9.0)
                .build();
        Scholarship unknown = Scholarship.builder()
                .id(103L)
                .name("Unverified Scholarship")
                .build();

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(10L)).thenReturn(Optional.of(profile));
        when(scholarshipRepository.findByActiveTrue()).thenReturn(List.of(eligible, ineligible, unknown));
        when(criteriaRepository.findByScholarshipIdIn(List.of(101L, 102L, 103L))).thenReturn(List.of());
        when(mlClient.rank(any(MlRecommendationRequest.class))).thenReturn(new MlRankingResponse(
                List.of(new MlRankingResponse.RankedScholarship(
                        101L,
                        0.91,
                        Map.of("autoencoder", 0.8, "semantic", 0.9, "clustering", 1.0, "graph", 0.7),
                        Map.of("kmeans", 1, "gmm", 0, "hdbscan", 2),
                        List.of("Highest matching signal: semantic"))),
                new MlRankingResponse.Pipeline(
                        "test",
                        1,
                        32,
                        "test-model",
                        "available",
                        Map.of(),
                        Map.of("semantic", 1.0))));

        StudentRecommendationService service = new StudentRecommendationService(
                userRepository,
                profileRepository,
                scholarshipRepository,
                criteriaRepository,
                new EligibilityEvaluationService(),
                mlClient);

        StudentRecommendations response = service.recommendForStudent("student@example.com");

        assertEquals(1, response.eligibleCandidateCount());
        assertEquals(List.of(101L), response.recommendations().stream()
                .map(recommendation -> recommendation.scholarshipId())
                .toList());
        verify(mlClient).rank(any(MlRecommendationRequest.class));
    }
}
