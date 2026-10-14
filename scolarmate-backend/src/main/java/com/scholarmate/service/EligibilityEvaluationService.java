package com.scholarmate.service;

import com.scholarmate.dto.EligibilityAssessment;
import com.scholarmate.entity.EligibilityCriteria;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.UserProfile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class EligibilityEvaluationService {

    public EligibilityAssessment evaluate(
            UserProfile profile,
            Scholarship scholarship,
            List<EligibilityCriteria> criteria) {

        if (profile == null || scholarship == null) {
            return EligibilityAssessment.builder()
                    .scholarshipId(scholarship != null ? scholarship.getId() : null)
                    .status("UNKNOWN")
                    .unknownCriteria(List.of("Student profile and scholarship details are required for evaluation."))
                    .evidence(List.of("Missing required input for eligibility evaluation."))
                    .build();
        }

        List<EligibilityCriteria> effectiveCriteria = new ArrayList<>();

        if (criteria != null) {
            effectiveCriteria.addAll(criteria);
        }

        effectiveCriteria.addAll(buildImplicitCriteriaFromScholarship(scholarship));

        if (effectiveCriteria.isEmpty()) {
            return EligibilityAssessment.builder()
                    .scholarshipId(scholarship.getId())
                    .status("UNKNOWN")
                    .unknownCriteria(List.of("No eligibility rules are available for this scholarship."))
                    .evidence(List.of("No eligibility criteria were defined for this scholarship."))
                    .build();
        }

        List<String> matchedCriteria = new ArrayList<>();
        List<String> failedCriteria = new ArrayList<>();
        List<String> unknownCriteria = new ArrayList<>();
        List<String> evidence = new ArrayList<>();

        for (EligibilityCriteria criterion : effectiveCriteria) {
            if (criterion == null) {
                continue;
            }

            EvaluationResult result = evaluateCriterion(profile, criterion);

            switch (result.state()) {
                case MATCHED -> {
                    matchedCriteria.add(criterion.getCriterionType());
                    evidence.add(result.message());
                }
                case FAILED -> {
                    failedCriteria.add(criterion.getCriterionType());
                    evidence.add(result.message());
                }
                case UNKNOWN -> {
                    unknownCriteria.add(criterion.getCriterionType());
                    evidence.add(result.message());
                }
                default -> {
                    // no-op
                }
            }
        }

        String status = determineStatus(matchedCriteria, failedCriteria, unknownCriteria);

        return EligibilityAssessment.builder()
                .scholarshipId(scholarship.getId())
                .status(status)
                .matchedCriteria(matchedCriteria)
                .failedCriteria(failedCriteria)
                .unknownCriteria(unknownCriteria)
                .evidence(evidence)
                .build();
    }

    private EvaluationResult evaluateCriterion(UserProfile profile, EligibilityCriteria criterion) {
        String criterionType = normalize(criterion.getCriterionType());
        String operator = normalize(criterion.getOperator());
        String value = criterion.getValue();

        if (matchesIgnoreCase(criterion.getStatus(), "UNKNOWN")
            || matchesIgnoreCase(criterionType, "UNKNOWN")) {
            return new EvaluationResult(
                EvaluationState.UNKNOWN,
                "Eligibility criteria require verification from the source."
            );
        }

        if (criterionType.isBlank() || operator.isBlank()) {
            return new EvaluationResult(EvaluationState.UNKNOWN, "Missing criterion type or operator.");
        }

        if (matchesIgnoreCase(criterionType, "MINIMUM_CGPA", "MINIMUM_MARKS")) {
            return evaluateNumericValue(profile.getCgpa(), value, operator, "CGPA", criterionType);
        }

        if (matchesIgnoreCase(criterionType, "MAX_FAMILY_INCOME")) {
            return evaluateNumericValue(profile.getFamilyIncome(), value, operator, "family income", criterionType);
        }

        if (matchesIgnoreCase(criterionType, "MINIMUM_YEAR_OF_STUDY", "YEAR_OF_STUDY")) {
            return evaluateIntegerValue(profile.getYearOfStudy(), value, operator, "year of study", criterionType);
        }

        if (matchesIgnoreCase(criterionType, "MAX_YEAR_OF_STUDY")) {
            return evaluateIntegerValue(profile.getYearOfStudy(), value, operator, "year of study", criterionType);
        }

        if (matchesIgnoreCase(criterionType, "COURSE", "COURSE_LEVEL")) {
            return evaluateTextValue(profile.getCourse(), value, operator, "course", criterionType);
        }

        if (matchesIgnoreCase(criterionType, "STATE")) {
            return evaluateTextValue(profile.getState(), value, operator, "state", criterionType);
        }

        if (matchesIgnoreCase(criterionType, "CATEGORY", "PERSONNEL_CATEGORY")) {
            return evaluateTextValue(profile.getCategory(), value, operator, "category", criterionType);
        }

        return new EvaluationResult(EvaluationState.UNKNOWN, "Unsupported criterion type: " + criterionType);
    }

    private EvaluationResult evaluateNumericValue(
            Double studentValue,
            String expectedValue,
            String operator,
            String label,
            String criterionType) {

        if (studentValue == null || expectedValue == null || expectedValue.isBlank()) {
            return new EvaluationResult(
                    EvaluationState.UNKNOWN,
                    "Missing " + label + " data for criterion " + criterionType + "."
            );
        }

        double required = parseDouble(expectedValue);
        if (Double.isNaN(required)) {
            return new EvaluationResult(
                    EvaluationState.UNKNOWN,
                    "Unable to parse required value for criterion " + criterionType + "."
            );
        }

        boolean matched = switch (operator) {
            case "GREATER_THAN" -> studentValue > required;
            case "GREATER_THAN_OR_EQUAL" -> studentValue >= required;
            case "LESS_THAN" -> studentValue < required;
            case "LESS_THAN_OR_EQUAL" -> studentValue <= required;
            case "EQUALS" -> Double.compare(studentValue, required) == 0;
            case "NOT_EQUALS" -> Double.compare(studentValue, required) != 0;
            default -> false;
        };

        if (matched) {
            return new EvaluationResult(EvaluationState.MATCHED, label + " matches requirement " + criterionType + ".");
        }

        return new EvaluationResult(EvaluationState.FAILED, label + " does not meet requirement " + criterionType + ".");
    }

    private EvaluationResult evaluateIntegerValue(
            Integer studentValue,
            String expectedValue,
            String operator,
            String label,
            String criterionType) {

        if (studentValue == null || expectedValue == null || expectedValue.isBlank()) {
            return new EvaluationResult(
                    EvaluationState.UNKNOWN,
                    "Missing " + label + " data for criterion " + criterionType + "."
            );
        }

        int required = parseInteger(expectedValue);
        if (required == Integer.MIN_VALUE) {
            return new EvaluationResult(
                    EvaluationState.UNKNOWN,
                    "Unable to parse required value for criterion " + criterionType + "."
            );
        }

        boolean matched = switch (operator) {
            case "GREATER_THAN" -> studentValue > required;
            case "GREATER_THAN_OR_EQUAL" -> studentValue >= required;
            case "LESS_THAN" -> studentValue < required;
            case "LESS_THAN_OR_EQUAL" -> studentValue <= required;
            case "EQUALS" -> Objects.equals(studentValue, required);
            case "NOT_EQUALS" -> !Objects.equals(studentValue, required);
            default -> false;
        };

        if (matched) {
            return new EvaluationResult(EvaluationState.MATCHED, label + " matches requirement " + criterionType + ".");
        }

        return new EvaluationResult(EvaluationState.FAILED, label + " does not meet requirement " + criterionType + ".");
    }

    private EvaluationResult evaluateTextValue(
            String studentValue,
            String expectedValue,
            String operator,
            String label,
            String criterionType) {

        if (studentValue == null || studentValue.isBlank()) {
            return new EvaluationResult(EvaluationState.UNKNOWN, "Missing " + label + " data for criterion " + criterionType + ".");
        }

        if (expectedValue == null || expectedValue.isBlank()) {
            return new EvaluationResult(EvaluationState.UNKNOWN, "Missing required value for criterion " + criterionType + ".");
        }

        String normalizedStudent = normalize(studentValue).toLowerCase();
        String normalizedExpected = normalize(expectedValue).toLowerCase();
        boolean matched = switch (operator) {
            case "EQUALS" -> normalizedStudent.equals(normalizedExpected);
            case "NOT_EQUALS" -> !normalizedStudent.equals(normalizedExpected);
            case "IN" -> containsAny(normalizedStudent, normalizedExpected);
            case "CONTAINS" -> normalizedStudent.contains(normalizedExpected);
            default -> false;
        };

        if (matched) {
            return new EvaluationResult(EvaluationState.MATCHED, label + " matches requirement " + criterionType + ".");
        }

        return new EvaluationResult(EvaluationState.FAILED, label + " does not match requirement " + criterionType + ".");
    }

    private List<EligibilityCriteria> buildImplicitCriteriaFromScholarship(Scholarship scholarship) {
        List<EligibilityCriteria> implicitRules = new ArrayList<>();

        if (scholarship.getMinimumCgpa() != null) {
            implicitRules.add(criteriaFor("MINIMUM_CGPA", "GREATER_THAN_OR_EQUAL", String.valueOf(scholarship.getMinimumCgpa())));
        }

        if (scholarship.getMaximumFamilyIncome() != null) {
            implicitRules.add(criteriaFor("MAX_FAMILY_INCOME", "LESS_THAN_OR_EQUAL", String.valueOf(scholarship.getMaximumFamilyIncome())));
        }

        if (scholarship.getCourse() != null && !scholarship.getCourse().isBlank()) {
            implicitRules.add(criteriaFor("COURSE", "CONTAINS", scholarship.getCourse()));
        }

        if (scholarship.getState() != null && !scholarship.getState().isBlank()) {
            implicitRules.add(criteriaFor("STATE", "IN", scholarship.getState()));
        }

        if (scholarship.getCategory() != null && !scholarship.getCategory().isBlank()) {
            implicitRules.add(criteriaFor("CATEGORY", "IN", scholarship.getCategory()));
        }

        if (scholarship.getMinimumYearOfStudy() != null) {
            implicitRules.add(criteriaFor("MINIMUM_YEAR_OF_STUDY", "GREATER_THAN_OR_EQUAL", String.valueOf(scholarship.getMinimumYearOfStudy())));
        }

        if (scholarship.getMaximumYearOfStudy() != null) {
            implicitRules.add(criteriaFor("MAX_YEAR_OF_STUDY", "LESS_THAN_OR_EQUAL", String.valueOf(scholarship.getMaximumYearOfStudy())));
        }

        return implicitRules;
    }

    private EligibilityCriteria criteriaFor(String criterionType, String operator, String value) {
        EligibilityCriteria criteria = new EligibilityCriteria();
        criteria.setCriterionType(criterionType);
        criteria.setOperator(operator);
        criteria.setValue(value);
        criteria.setStatus("KNOWN");
        criteria.setEvidence("Derived from scholarship metadata.");
        return criteria;
    }

    private String determineStatus(
            List<String> matchedCriteria,
            List<String> failedCriteria,
            List<String> unknownCriteria) {

        if (!unknownCriteria.isEmpty()) {
            return "UNKNOWN";
        }

        if (!failedCriteria.isEmpty()) {
            return "NOT_ELIGIBLE";
        }

        if (!matchedCriteria.isEmpty()) {
            return "ELIGIBLE";
        }

        return "UNKNOWN";
    }

    private boolean containsAny(String studentValue, String expectedValues) {
        String[] parts = expectedValues.split("[,;|]");
        for (String part : parts) {
            if (studentValue.equals(part.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesIgnoreCase(String actual, String... expectedValues) {
        if (actual == null) {
            return false;
        }
        for (String expected : expectedValues) {
            if (actual.equalsIgnoreCase(expected)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.replace(",", ""));
        } catch (Exception ex) {
            return Double.NaN;
        }
    }

    private int parseInteger(String value) {
        try {
            return Integer.parseInt(value.replace(",", ""));
        } catch (Exception ex) {
            return Integer.MIN_VALUE;
        }
    }

    private enum EvaluationState {
        MATCHED,
        FAILED,
        UNKNOWN
    }

    private record EvaluationResult(EvaluationState state, String message) {
    }
}
