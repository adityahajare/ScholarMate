package com.scholarmate.discovery.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarmate.discovery.client.MlAdaptiveRankingClient;
import com.scholarmate.discovery.dto.*;
import com.scholarmate.discovery.dto.AdaptiveQuestionDto.OptionDto;
import com.scholarmate.discovery.dto.AdaptiveQuestionRankingResponseDto.RankedQuestionDto;
import com.scholarmate.discovery.dto.CandidateAnalysisResult;
import com.scholarmate.entity.User;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.form.model.*;
import com.scholarmate.form.repository.*;
import com.scholarmate.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdaptiveDiscoveryService {

    private final DiscoverySessionRepository sessionRepo;
    private final DiscoverySessionAnswerRepository answerRepo;
    private final MasterFieldRepository masterFieldRepo;
    private final FieldOptionSetRepository optionSetRepo;
    private final FieldOptionRepository optionRepo;
    private final ConditionalRuleRepository ruleRepo;
    private final UserProfileRepository userProfileRepo;
    private final ScholarshipCandidateService candidateService;
    private final MlAdaptiveRankingClient rankingClient;
    private final AdaptiveEligibilityEvaluator eligibilityEvaluator;
    private final ObjectMapper objectMapper;

    public AdaptiveDiscoveryService(DiscoverySessionRepository sessionRepo,
                                    DiscoverySessionAnswerRepository answerRepo,
                                    MasterFieldRepository masterFieldRepo,
                                    FieldOptionSetRepository optionSetRepo,
                                    FieldOptionRepository optionRepo,
                                    ConditionalRuleRepository ruleRepo,
                                    UserProfileRepository userProfileRepo,
                                    ScholarshipCandidateService candidateService,
                                    MlAdaptiveRankingClient rankingClient,
                                    AdaptiveEligibilityEvaluator eligibilityEvaluator) {
        super();
        this.sessionRepo = sessionRepo;
        this.answerRepo = answerRepo;
        this.masterFieldRepo = masterFieldRepo;
        this.optionSetRepo = optionSetRepo;
        this.optionRepo = optionRepo;
        this.ruleRepo = ruleRepo;
        this.userProfileRepo = userProfileRepo;
        this.candidateService = candidateService;
        this.rankingClient = rankingClient;
        this.eligibilityEvaluator = eligibilityEvaluator;
        this.objectMapper = new ObjectMapper();
    }

    private static final int BATCH_SIZE = 2; // Return 1-2 focused questions at a time

    @Transactional
    public DiscoverySessionResponseDto startOrResumeSession(User user) {
        UserProfile profile = userProfileRepo.findByUser(user).orElse(null);

        // Resume active session if exists
        DiscoverySession session = sessionRepo.findFirstByUserAndSessionStatusOrderByUpdatedAtDesc(user, "IN_PROGRESS")
                .orElseGet(() -> {
                    DiscoverySession newSession = new DiscoverySession();
                    newSession.setUser(user);
                    newSession.setSessionStatus("IN_PROGRESS");
                    newSession.setDatasetVersion("v1.0");
                    newSession.setCreatedAt(OffsetDateTime.now());
                    newSession.setUpdatedAt(OffsetDateTime.now());
                    return sessionRepo.save(newSession);
                });

        return buildSessionResponse(session, profile);
    }

    @Transactional(readOnly = true)
    public DiscoverySessionResponseDto getSessionQuestions(Long sessionId, User user) {
        DiscoverySession session = getAuthorizedSession(sessionId, user);
        UserProfile profile = userProfileRepo.findByUser(user).orElse(null);
        return buildSessionResponse(session, profile);
    }

    @Transactional
    public DiscoverySessionResponseDto submitAnswers(Long sessionId, User user, Map<String, Object> newAnswers) {
        DiscoverySession session = getAuthorizedSession(sessionId, user);
        UserProfile profile = userProfileRepo.findByUser(user).orElse(null);

        // 1. Validate answers against master field definitions
        for (Map.Entry<String, Object> entry : newAnswers.entrySet()) {
            String fieldKey = entry.getKey();
            Object rawVal = entry.getValue();

            MasterField mf = masterFieldRepo.findByFieldKey(fieldKey)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown input parameter field: " + fieldKey));

            validateAnswer(mf, rawVal);

            // Persist into session answers
            DiscoverySessionAnswer answer = answerRepo.findBySessionAndFieldKey(session, fieldKey)
                    .orElseGet(() -> {
                        DiscoverySessionAnswer a = new DiscoverySessionAnswer();
                        a.setSession(session);
                        a.setFieldKey(fieldKey);
                        return a;
                    });
            answer.setAnswerValue(rawVal != null ? rawVal.toString() : null);
            answer.setAnsweredAt(OffsetDateTime.now());
            answerRepo.save(answer);

            // Persist back to UserProfile.profileData for long term profile building
            if (profile != null) {
                if (profile.getProfileData() == null) {
                    profile.setProfileData(new HashMap<>());
                }
                profile.getProfileData().put(fieldKey, rawVal);
                syncToStandardProfileColumns(profile, fieldKey, rawVal);
            }
        }

        if (profile != null) {
            userProfileRepo.save(profile);
        }

        session.setUpdatedAt(OffsetDateTime.now());
        sessionRepo.save(session);

        return buildSessionResponse(session, profile);
    }

    @Transactional(readOnly = true)
    public List<ScholarshipMatchResponseDto> getMatches(Long sessionId, User user) {
        DiscoverySession session = getAuthorizedSession(sessionId, user);
        UserProfile profile = userProfileRepo.findByUser(user).orElse(null);
        Map<String, Object> answers = loadSessionAnswersMap(session);
        return eligibilityEvaluator.evaluate(profile, answers);
    }

    @Transactional(readOnly = true)
    public DiscoveryProgressDto getProgress(Long sessionId, User user) {
        DiscoverySession session = getAuthorizedSession(sessionId, user);
        UserProfile profile = userProfileRepo.findByUser(user).orElse(null);
        Map<String, Object> answers = loadSessionAnswersMap(session);

        CandidateAnalysisResult analysis = candidateService.analyzeCandidates(profile, answers);
        List<ScholarshipMatchResponseDto> matches = eligibilityEvaluator.evaluate(profile, answers);

        int potentiallyEligible = 0;
        int needsMore = 0;
        int notEligible = 0;
        int pending = 0;

        for (ScholarshipMatchResponseDto m : matches) {
            switch (m.getEvaluationStatus()) {
                case "POTENTIALLY_ELIGIBLE" -> potentiallyEligible++;
                case "NEEDS_MORE_INFORMATION" -> needsMore++;
                case "NOT_ELIGIBLE" -> notEligible++;
                case "PENDING_VERIFICATION" -> pending++;
            }
        }

        int answeredCount = answers.size();
        int remainingCount = analysis.getMissingFieldKeys().size();
        double progress = (answeredCount + remainingCount > 0)
                ? ((double) answeredCount / (answeredCount + remainingCount)) * 100.0
                : 100.0;

        return DiscoveryProgressDto.builder()
                .sessionId(session.getId())
                .status(session.getSessionStatus())
                .totalCandidatesCount(analysis.getCandidateScholarshipKeys().size())
                .potentiallyEligibleCount(potentiallyEligible)
                .needsMoreInfoCount(needsMore)
                .notEligibleCount(notEligible)
                .pendingVerificationCount(pending)
                .questionsAnsweredCount(answeredCount)
                .remainingQuestionsCount(remainingCount)
                .progressPercentage(Math.round(progress * 10.0) / 10.0)
                .answeredFieldKeys(new ArrayList<>(answers.keySet()))
                .remainingFieldKeys(analysis.getMissingFieldKeys())
                .build();
    }

    private DiscoverySessionResponseDto buildSessionResponse(DiscoverySession session, UserProfile profile) {
        Map<String, Object> currentAnswers = loadSessionAnswersMap(session);

        // 1. Analyze candidates and missing parameters
        CandidateAnalysisResult analysis = candidateService.analyzeCandidates(profile, currentAnswers);

        // 2. Request question ranking from ML Client
        AdaptiveQuestionRankingRequestDto rankingReq = AdaptiveQuestionRankingRequestDto.builder()
                .studentId(session.getUser().getId().toString())
                .candidateScholarshipIds(analysis.getCandidateScholarshipKeys())
                .knownFieldKeys(new ArrayList<>(analysis.getKnownFieldKeys()))
                .missingFieldKeys(analysis.getMissingFieldKeys())
                .alreadyAskedFieldKeys(new ArrayList<>(currentAnswers.keySet()))
                .scholarshipRequirementFieldMap(analysis.getRequirementFieldMap())
                .scholarshipNames(analysis.getScholarshipNames())
                .build();

        AdaptiveQuestionRankingResponseDto rankingResp = rankingClient.rankQuestions(rankingReq);

        // 3. Apply Deterministic Safeguards & build Question DTOs
        List<AdaptiveQuestionDto> nextQuestions = new ArrayList<>();
        Map<String, MasterField> fieldCache = masterFieldRepo.findAll().stream()
                .collect(Collectors.toMap(MasterField::getFieldKey, f -> f));

        for (RankedQuestionDto rq : rankingResp.getRankedQuestions()) {
            if (nextQuestions.size() >= BATCH_SIZE) {
                break;
            }

            MasterField mf = fieldCache.get(rq.getFieldKey());
            if (mf == null) {
                continue; // Guard: Ignore fields not in master catalogue
            }

            // Guard: Check conditional display rules
            if (!isConditionSatisfied(mf, currentAnswers)) {
                continue;
            }

            AdaptiveQuestionDto qDto = AdaptiveQuestionDto.builder()
                    .fieldKey(mf.getFieldKey())
                    .label(mf.getLabel())
                    .section(mf.getSection())
                    .dataType(mf.getDataType())
                    .required(mf.getDefaultRequired())
                    .validationRule(mf.getValidationRule())
                    .displayCondition(mf.getDisplayCondition())
                    .priorityScore(rq.getPriorityScore())
                    .reason(rq.getReason())
                    .options(loadOptions(mf.getOptionsKey()))
                    .build();

            nextQuestions.add(qDto);
        }

        boolean sufficient = nextQuestions.isEmpty() || analysis.getMissingFieldKeys().isEmpty();
        if (sufficient && !"COMPLETED".equals(session.getSessionStatus())) {
            session.setSessionStatus("COMPLETED");
            sessionRepo.save(session);
        }

        return DiscoverySessionResponseDto.builder()
                .sessionId(session.getId())
                .status(session.getSessionStatus())
                .candidateScholarshipCount(analysis.getCandidateScholarshipKeys().size())
                .questionsAnsweredCount(currentAnswers.size())
                .remainingQuestionsCount(analysis.getMissingFieldKeys().size())
                .hasSufficientInformation(sufficient)
                .nextQuestions(nextQuestions)
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    private boolean isConditionSatisfied(MasterField mf, Map<String, Object> answers) {
        String dispCond = mf.getDisplayCondition();
        if (dispCond == null || "ALWAYS".equalsIgnoreCase(dispCond) || "IF_REQUIRED".equalsIgnoreCase(dispCond)) {
            return true;
        }

        if ("ACCOMMODATION_HOSTELLER".equalsIgnoreCase(dispCond)) {
            Object acc = answers.get("accommodation_type");
            return acc != null && "HOSTELLER".equalsIgnoreCase(acc.toString());
        }

        if ("DISABILITY_STATUS_TRUE".equalsIgnoreCase(dispCond)) {
            Object dis = answers.get("disability_status");
            return dis != null && ("true".equalsIgnoreCase(dis.toString()) || Boolean.TRUE.equals(dis));
        }

        if ("EDUCATION_SELECTED".equalsIgnoreCase(dispCond)) {
            Object edu = answers.get("education_level");
            return edu != null && !edu.toString().trim().isEmpty();
        }

        return true;
    }

    private List<OptionDto> loadOptions(String optionsKey) {
        if (optionsKey == null || optionsKey.isBlank()) {
            return Collections.emptyList();
        }
        return optionSetRepo.findByOptionKey(optionsKey)
                .map(set -> optionRepo.findAllByOptionSetAndActiveTrueOrderBySortOrderAsc(set).stream()
                        .map(o -> new OptionDto(o.getOptionValue(), o.getOptionLabel()))
                        .toList())
                .orElse(Collections.emptyList());
    }

    private void validateAnswer(MasterField mf, Object value) {
        if (value == null || value.toString().trim().isEmpty()) {
            if (mf.getDefaultRequired()) {
                throw new IllegalArgumentException("Field '" + mf.getLabel() + "' is required.");
            }
            return;
        }

        String str = value.toString().trim();
        String type = mf.getDataType().toUpperCase();

        switch (type) {
            case "NUMBER" -> {
                try {
                    double num = Double.parseDouble(str);
                    if ("NON_NEGATIVE".equalsIgnoreCase(mf.getValidationRule()) && num < 0) {
                        throw new IllegalArgumentException(mf.getLabel() + " cannot be negative.");
                    }
                    if ("RANGE_0_TO_100".equalsIgnoreCase(mf.getValidationRule()) && (num < 0 || num > 100)) {
                        throw new IllegalArgumentException(mf.getLabel() + " must be between 0 and 100.");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(mf.getLabel() + " must be a valid number.");
                }
            }
            case "BOOLEAN" -> {
                if (!str.equalsIgnoreCase("true") && !str.equalsIgnoreCase("false")) {
                    throw new IllegalArgumentException(mf.getLabel() + " must be either true or false.");
                }
            }
            case "DROPDOWN" -> {
                if (mf.getOptionsKey() != null) {
                    List<OptionDto> validOptions = loadOptions(mf.getOptionsKey());
                    if (!validOptions.isEmpty()) {
                        boolean match = validOptions.stream().anyMatch(o -> o.getValue().equalsIgnoreCase(str));
                        if (!match) {
                            throw new IllegalArgumentException("Invalid option selected for " + mf.getLabel() + ": " + str);
                        }
                    }
                }
            }
        }
    }

    private void syncToStandardProfileColumns(UserProfile profile, String key, Object val) {
        if (val == null) return;
        String s = val.toString().trim();
        switch (key) {
            case "annual_family_income" -> {
                try { profile.setFamilyIncome(Double.parseDouble(s)); } catch (Exception ignored) {}
            }
            case "domicile_state" -> profile.setDomicileState(s);
            case "caste_category" -> profile.setCategory(s);
            case "education_level" -> profile.setEducationStage(s);
            case "course_name" -> profile.setCourse(s);
            case "institution_name" -> profile.setCollege(s);
            case "permanent_state" -> profile.setState(s);
            case "current_year" -> {
                try { profile.setYearOfStudy(Integer.parseInt(s)); } catch (Exception ignored) {}
            }
            case "gender" -> profile.setGender(s);
        }
    }

    private Map<String, Object> loadSessionAnswersMap(DiscoverySession session) {
        Map<String, Object> map = new HashMap<>();
        List<DiscoverySessionAnswer> answers = answerRepo.findAllBySession(session);
        for (DiscoverySessionAnswer a : answers) {
            map.put(a.getFieldKey(), a.getAnswerValue());
        }
        return map;
    }

    private DiscoverySession getAuthorizedSession(Long sessionId, User user) {
        DiscoverySession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Discovery session not found: " + sessionId));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Unauthorized: You cannot access another student's discovery session.");
        }
        return session;
    }
}
