package com.scholarmate.discovery.service;

import com.scholarmate.discovery.client.MlAdaptiveRankingClient;
import com.scholarmate.discovery.dto.*;
import com.scholarmate.entity.User;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.form.model.*;
import com.scholarmate.form.repository.*;
import com.scholarmate.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdaptiveDiscoveryServiceTest {

    @Mock
    private DiscoverySessionRepository sessionRepo;

    @Mock
    private DiscoverySessionAnswerRepository answerRepo;

    @Mock
    private MasterFieldRepository masterFieldRepo;

    @Mock
    private FieldOptionSetRepository optionSetRepo;

    @Mock
    private FieldOptionRepository optionRepo;

    @Mock
    private ConditionalRuleRepository ruleRepo;

    @Mock
    private UserProfileRepository userProfileRepo;

    @Mock
    private ScholarshipCandidateService candidateService;

    @Mock
    private MlAdaptiveRankingClient rankingClient;

    @Mock
    private AdaptiveEligibilityEvaluator eligibilityEvaluator;

    @InjectMocks
    private AdaptiveDiscoveryService discoveryService;

    private User student1;
    private User student2;
    private DiscoverySession session;
    private MasterField incomeField;
    private MasterField domicileField;

    @BeforeEach
    void setUp() {
        student1 = new User();
        student1.setId(101L);
        student1.setEmail("student1@example.com");

        student2 = new User();
        student2.setId(202L);
        student2.setEmail("student2@example.com");

        session = new DiscoverySession();
        session.setId(1L);
        session.setUser(student1);
        session.setSessionStatus("IN_PROGRESS");
        session.setCreatedAt(OffsetDateTime.now());
        session.setUpdatedAt(OffsetDateTime.now());

        incomeField = new MasterField();
        incomeField.setId(1L);
        incomeField.setFieldKey("annual_family_income");
        incomeField.setLabel("Annual Family Income");
        incomeField.setSection("FAMILY");
        incomeField.setDataType("NUMBER");
        incomeField.setDefaultRequired(false);
        incomeField.setDisplayCondition("ALWAYS");
        incomeField.setValidationRule("NON_NEGATIVE");

        domicileField = new MasterField();
        domicileField.setId(2L);
        domicileField.setFieldKey("domicile_state");
        domicileField.setLabel("Domicile State");
        domicileField.setSection("ADDRESS");
        domicileField.setDataType("DROPDOWN");
        domicileField.setDefaultRequired(false);
        domicileField.setDisplayCondition("ALWAYS");
        domicileField.setValidationRule("VALID_OPTION");
    }

    @Test
    @DisplayName("Scenario A: Start session prioritizes missing critical fields like income and domicile")
    void testStartSessionPrioritizesMissingFields() {
        when(userProfileRepo.findByUser(student1)).thenReturn(Optional.empty());
        when(sessionRepo.findFirstByUserAndSessionStatusOrderByUpdatedAtDesc(student1, "IN_PROGRESS"))
                .thenReturn(Optional.of(session));
        when(answerRepo.findAllBySession(session)).thenReturn(Collections.emptyList());

        CandidateAnalysisResult analysisResult =
                CandidateAnalysisResult.builder()
                        .candidateScholarshipKeys(List.of("MAHA_EBC_01"))
                        .missingFieldKeys(List.of("annual_family_income", "domicile_state"))
                        .requirementFieldMap(Map.of("annual_family_income", List.of("MAHA_EBC_01")))
                        .scholarshipNames(Map.of("MAHA_EBC_01", "Maha EBC"))
                        .knownFieldKeys(Set.of())
                        .build();

        when(candidateService.analyzeCandidates(any(), any())).thenReturn(analysisResult);

        AdaptiveQuestionRankingResponseDto rankingResponse = AdaptiveQuestionRankingResponseDto.builder()
                .rankedQuestions(List.of(
                        new AdaptiveQuestionRankingResponseDto.RankedQuestionDto(
                                "annual_family_income", 0.95, "Resolves family income eligibility", 1),
                        new AdaptiveQuestionRankingResponseDto.RankedQuestionDto(
                                "domicile_state", 0.80, "Resolves state domicile", 1)
                ))
                .build();

        when(rankingClient.rankQuestions(any())).thenReturn(rankingResponse);
        when(masterFieldRepo.findAll()).thenReturn(List.of(incomeField, domicileField));

        DiscoverySessionResponseDto response = discoveryService.startOrResumeSession(student1);

        assertNotNull(response);
        assertEquals(1L, response.getSessionId());
        assertEquals(2, response.getNextQuestions().size());
        assertEquals("annual_family_income", response.getNextQuestions().get(0).getFieldKey());
        assertEquals("Annual Family Income", response.getNextQuestions().get(0).getLabel());
    }

    @Test
    @DisplayName("Scenario C: Unauthorized user cannot access another student's session")
    void testUnauthorizedSessionAccess() {
        when(sessionRepo.findById(1L)).thenReturn(Optional.of(session));

        assertThrows(AccessDeniedException.class, () -> {
            discoveryService.getSessionQuestions(1L, student2);
        });
    }

    @Test
    @DisplayName("Scenario D: Negative income value is rejected by deterministic validator")
    void testNegativeIncomeValidationFailure() {
        when(sessionRepo.findById(1L)).thenReturn(Optional.of(session));
        when(userProfileRepo.findByUser(student1)).thenReturn(Optional.empty());
        when(masterFieldRepo.findByFieldKey("annual_family_income")).thenReturn(Optional.of(incomeField));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            discoveryService.submitAnswers(1L, student1, Map.of("annual_family_income", -50000));
        });

        assertTrue(ex.getMessage().contains("cannot be negative"));
    }

    @Test
    @DisplayName("Scenario F: Match evaluation correctly flags POTENTIALLY_ELIGIBLE and NEEDS_MORE_INFORMATION")
    void testEligibilityEvaluationStatus() {
        when(sessionRepo.findById(1L)).thenReturn(Optional.of(session));
        when(userProfileRepo.findByUser(student1)).thenReturn(Optional.empty());
        when(answerRepo.findAllBySession(session)).thenReturn(Collections.emptyList());

        List<ScholarshipMatchResponseDto> mockMatches = List.of(
                ScholarshipMatchResponseDto.builder()
                        .scholarshipKey("MAHA_EBC_01")
                        .scholarshipName("Maha EBC")
                        .evaluationStatus("POTENTIALLY_ELIGIBLE")
                        .matchConfidence(0.95)
                        .build()
        );

        when(eligibilityEvaluator.evaluate(any(), any())).thenReturn(mockMatches);

        List<ScholarshipMatchResponseDto> matches = discoveryService.getMatches(1L, student1);

        assertNotNull(matches);
        assertEquals(1, matches.size());
        assertEquals("POTENTIALLY_ELIGIBLE", matches.get(0).getEvaluationStatus());
    }
}
