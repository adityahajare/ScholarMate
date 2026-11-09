package com.scholarmate.discovery.service;

import com.scholarmate.discovery.dto.ScholarshipMatchResponseDto;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.form.model.ScholarshipRequirement;
import com.scholarmate.form.repository.ScholarshipRequirementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
public class AdaptiveEligibilityEvaluator {

    private final ScholarshipRequirementRepository requirementRepo;

    public AdaptiveEligibilityEvaluator(ScholarshipRequirementRepository requirementRepo) {
        super();
        this.requirementRepo = requirementRepo;
    }

    public List<ScholarshipMatchResponseDto> evaluate(UserProfile profile, Map<String, Object> sessionAnswers) {
        Map<String, Object> known = new HashMap<>();
        if (profile != null) {
            extractProfileFields(profile, known);
            if (profile.getProfileData() != null) {
                known.putAll(profile.getProfileData());
            }
        }
        if (sessionAnswers != null) {
            known.putAll(sessionAnswers);
        }

        List<ScholarshipRequirement> allReqs = requirementRepo.findAll();
        Map<String, List<ScholarshipRequirement>> byScheme = new HashMap<>();
        for (ScholarshipRequirement req : allReqs) {
            byScheme.computeIfAbsent(req.getScholarshipKey(), k -> new ArrayList<>()).add(req);
        }

        List<ScholarshipMatchResponseDto> results = new ArrayList<>();

        for (Map.Entry<String, List<ScholarshipRequirement>> entry : byScheme.entrySet()) {
            String schKey = entry.getKey();
            List<ScholarshipRequirement> reqs = entry.getValue();

            String schName = reqs.get(0).getScholarshipName();
            String acadYear = reqs.get(0).getAcademicYear();
            String sourceUrl = reqs.get(0).getSourceUrl();
            String sourceDoc = reqs.get(0).getSourceDocument();
            String sourceSec = reqs.get(0).getSourceSection();

            List<String> satisfied = new ArrayList<>();
            List<String> failed = new ArrayList<>();
            List<String> missing = new ArrayList<>();
            List<String> documents = new ArrayList<>();
            boolean hasPendingVerification = false;

            for (ScholarshipRequirement req : reqs) {
                if (!"VERIFIED".equalsIgnoreCase(req.getVerificationStatus())) {
                    hasPendingVerification = true;
                }

                String fieldKey = req.getFieldKey();
                Object actualVal = known.get(fieldKey);

                if (actualVal == null || actualVal.toString().trim().isEmpty()) {
                    if (req.getMandatory()) {
                        missing.add(fieldKey + " (" + req.getConditionKey() + ")");
                    }
                    continue;
                }

                boolean passed = evaluateCondition(req.getOperator(), req.getExpectedValue(), actualVal.toString());
                if (passed) {
                    satisfied.add(req.getConditionKey() + ": Satisfied (" + req.getOperator() + " " + req.getExpectedValue() + ")");
                } else {
                    failed.add(req.getConditionKey() + ": Not met (Expected " + req.getOperator() + " " + req.getExpectedValue() + ", provided: " + actualVal + ")");
                }

                // If requirement relates to documents, track in required documents
                if (fieldKey.toLowerCase().contains("certificate") || fieldKey.toLowerCase().contains("marksheet")) {
                    documents.add(fieldKey);
                }
            }

            // Determine Status
            String status;
            double confidence;
            String summary;

            if (hasPendingVerification) {
                status = "PENDING_VERIFICATION";
                confidence = 0.50;
                summary = "One or more eligibility guidelines are awaiting official gazette verification. Verification in progress.";
            } else if (!failed.isEmpty()) {
                status = "NOT_ELIGIBLE";
                confidence = 1.0;
                summary = "One or more mandatory eligibility requirements were not met based on verified criteria.";
            } else if (!missing.isEmpty()) {
                status = "NEEDS_MORE_INFORMATION";
                confidence = 0.65;
                summary = "Potentially eligible, but additional required parameters must be provided before final evaluation.";
            } else {
                status = "POTENTIALLY_ELIGIBLE";
                confidence = 0.95;
                summary = "You satisfy all verified criteria for this scheme. Please note this indicates potential qualification; final award depends on official portal review and quota.";
            }

            results.add(ScholarshipMatchResponseDto.builder()
                    .scholarshipKey(schKey)
                    .scholarshipName(schName)
                    .academicYear(acadYear)
                    .evaluationStatus(status)
                    .matchConfidence(confidence)
                    .satisfiedConditions(satisfied)
                    .failedConditions(failed)
                    .missingFields(missing)
                    .requiredDocuments(documents)
                    .sourceUrl(sourceUrl)
                    .sourceDocument(sourceDoc)
                    .sourceSection(sourceSec)
                    .lastVerificationDate(LocalDate.now().toString())
                    .eligibilitySummary(summary)
                    .build());
        }

        // Sort: POTENTIALLY_ELIGIBLE first, then NEEDS_MORE_INFORMATION, then PENDING_VERIFICATION, then NOT_ELIGIBLE
        results.sort(Comparator.comparingInt(r -> switch (r.getEvaluationStatus()) {
            case "POTENTIALLY_ELIGIBLE" -> 1;
            case "NEEDS_MORE_INFORMATION" -> 2;
            case "PENDING_VERIFICATION" -> 3;
            case "NOT_ELIGIBLE" -> 4;
            default -> 5;
        }));

        return results;
    }

    private boolean evaluateCondition(String operator, String expected, String actual) {
        if (expected == null || actual == null) return false;
        String op = operator.toUpperCase().trim();
        String act = actual.trim();
        String exp = expected.trim();

        try {
            return switch (op) {
                case "EQUALS", "=" -> act.equalsIgnoreCase(exp);
                case "NOT_EQUALS", "!=" -> !act.equalsIgnoreCase(exp);
                case "IN" -> {
                    String clean = exp.replaceAll("[\\[\\]\"']", "");
                    List<String> items = Arrays.stream(clean.split(",")).map(String::trim).toList();
                    yield items.stream().anyMatch(i -> i.equalsIgnoreCase(act));
                }
                case "NOT_IN" -> {
                    String clean = exp.replaceAll("[\\[\\]\"']", "");
                    List<String> items = Arrays.stream(clean.split(",")).map(String::trim).toList();
                    yield items.stream().noneMatch(i -> i.equalsIgnoreCase(act));
                }
                case "LESS_THAN_OR_EQUAL", "<=" -> Double.parseDouble(act) <= Double.parseDouble(exp);
                case "GREATER_THAN_OR_EQUAL", ">=" -> Double.parseDouble(act) >= Double.parseDouble(exp);
                case "LESS_THAN", "<" -> Double.parseDouble(act) < Double.parseDouble(exp);
                case "GREATER_THAN", ">" -> Double.parseDouble(act) > Double.parseDouble(exp);
                default -> true;
            };
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void extractProfileFields(UserProfile profile, Map<String, Object> map) {
        if (profile.getFullName() != null) map.put("full_name", profile.getFullName());
        if (profile.getEducationStage() != null) map.put("education_level", profile.getEducationStage());
        if (profile.getCourse() != null) map.put("course_name", profile.getCourse());
        if (profile.getCollege() != null) map.put("institution_name", profile.getCollege());
        if (profile.getState() != null) map.put("permanent_state", profile.getState());
        if (profile.getDomicileState() != null) map.put("domicile_state", profile.getDomicileState());
        if (profile.getCategory() != null) map.put("caste_category", profile.getCategory());
        if (profile.getFamilyIncome() != null) map.put("annual_family_income", profile.getFamilyIncome());
        if (profile.getCgpa() != null) map.put("previous_percentage", profile.getCgpa() * 9.5);
        if (profile.getYearOfStudy() != null) map.put("current_year", String.valueOf(profile.getYearOfStudy()));
        if (profile.getGender() != null) map.put("gender", profile.getGender());
    }
}
