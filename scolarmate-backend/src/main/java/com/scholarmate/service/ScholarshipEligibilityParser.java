package com.scholarmate.service;

import com.scholarmate.entity.EligibilityCriteria;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.repository.EligibilityCriteriaRepository;
import com.scholarmate.repository.ScholarshipRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScholarshipEligibilityParser {

    private final EligibilityCriteriaRepository criteriaRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final ScholarshipSectionExtractor sectionExtractor;
        private final EligibilityCriteriaDefaultsService defaultsService;

    public ScholarshipEligibilityParser(
            EligibilityCriteriaRepository criteriaRepository,
            ScholarshipRepository scholarshipRepository,
                        ScholarshipSectionExtractor sectionExtractor,
                        EligibilityCriteriaDefaultsService defaultsService) {

        this.criteriaRepository = criteriaRepository;
        this.scholarshipRepository = scholarshipRepository;
        this.sectionExtractor = sectionExtractor;
                this.defaultsService = defaultsService;
    }

    public List<EligibilityCriteria> parseAndSave(
            Long scholarshipId,
            String rawText) {

        Scholarship scholarship =
                scholarshipRepository.findById(scholarshipId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Scholarship not found"
                                ));

        Map<String, String> sections =
                sectionExtractor.extractSections(rawText);

        String eligibility =
                sections.get("ELIGIBILITY");

        if (eligibility == null || eligibility.isBlank()) {
            throw new RuntimeException(
                    "Eligibility section not found"
            );
        }

        List<EligibilityCriteria> results =
                new ArrayList<>();

        /*
         * Minimum marks - new applicants
         */
        Matcher minimumMarks =
                Pattern.compile(
                        "(?i)minimum\\s+(?:60|sixty)%\\s+marks"
                ).matcher(eligibility);

        if (minimumMarks.find()) {

            results.add(
                    createCriterion(
                            scholarship,
                            "MINIMUM_MARKS",
                            "GREATER_THAN_OR_EQUAL",
                            "60",
                            null,
                            "KNOWN",
                            minimumMarks.group()
                    )
            );
        }

        /*
         * Renewal marks
         */
        Matcher renewalMarks =
                Pattern.compile(
                        "(?i)renewal.{0,250}?minimum\\s+50%\\s+marks"
                ).matcher(eligibility);

        if (renewalMarks.find()) {

            results.add(
                    createCriterion(
                            scholarship,
                            "RENEWAL_MINIMUM_MARKS",
                            "GREATER_THAN_OR_EQUAL",
                            "50",
                            null,
                            "KNOWN",
                            renewalMarks.group()
                    )
            );
        }

        /*
         * CAPFs / Assam Rifles
         */
        if (containsAny(
                eligibility,
                "CAPFs",
                "Assam Rifles")) {

            results.add(
                    createCriterion(
                            scholarship,
                            "PERSONNEL_CATEGORY",
                            "IN",
                            "CAPFs;Assam Rifles",
                            null,
                            "KNOWN",
                            "Eligibility section mentions CAPFs and Assam Rifles personnel."
                    )
            );
        }

        /*
         * States / UT Police
         */
        if (containsAny(
                eligibility,
                "States/UTs Police",
                "States / UTs Police")) {

            results.add(
                    createCriterion(
                            scholarship,
                            "PERSONNEL_CATEGORY",
                            "IN",
                            "States/UTs Police",
                            null,
                            "KNOWN",
                            "Eligibility section mentions dependent wards of States/UTs Police personnel."
                    )
            );
        }

        /*
         * First professional degree
         */
        if (containsIgnoreCase(
                eligibility,
                "first professional degree")) {

            results.add(
                    createCriterion(
                            scholarship,
                            "COURSE_LEVEL",
                            "EQUALS",
                            "FIRST_PROFESSIONAL_DEGREE",
                            null,
                            "KNOWN",
                            "Eligibility requires pursuing a first professional degree programme."
                    )
            );
        }

        /*
         * Maximum children per family
         */
        Matcher children =
                Pattern.compile(
                        "(?i)only\\s+(\\d+)\\s+Children\\s+per\\s+family"
                ).matcher(eligibility);

        if (children.find()) {

            results.add(
                    createCriterion(
                            scholarship,
                            "MAX_CHILDREN_PER_FAMILY",
                            "LESS_THAN_OR_EQUAL",
                            children.group(1),
                            null,
                            "KNOWN",
                            children.group()
                    )
            );
        }

        /*
         * Duration
         */
        Matcher duration =
                Pattern.compile(
                        "(?i)up\\s+to\\s+(\\d+)\\s+years"
                ).matcher(eligibility);

        if (duration.find()) {

            results.add(
                    createCriterion(
                            scholarship,
                            "MAX_DURATION_YEARS",
                            "LESS_THAN_OR_EQUAL",
                            duration.group(1),
                            null,
                            "KNOWN",
                            duration.group()
                    )
            );
        }

                if (results.isEmpty()) {
                        return List.of(defaultsService.ensurePlaceholder(scholarship));
                }

                defaultsService.removePlaceholder(scholarshipId);
                List<EligibilityCriteria> existingCriteria =
                                criteriaRepository.findByScholarshipId(scholarshipId);
                List<EligibilityCriteria> persistedResults = new ArrayList<>();

                for (EligibilityCriteria parsedCriterion : results) {
                        EligibilityCriteria criterion = existingCriteria.stream()
                                        .filter(existing -> hasSameRule(existing, parsedCriterion))
                                        .findFirst()
                                        .orElse(parsedCriterion);

                        if (criterion != parsedCriterion) {
                                criterion.setOperator(parsedCriterion.getOperator());
                                criterion.setValue(parsedCriterion.getValue());
                                criterion.setValue2(parsedCriterion.getValue2());
                                criterion.setStatus(parsedCriterion.getStatus());
                                criterion.setEvidence(parsedCriterion.getEvidence());
                        }
                        persistedResults.add(criteriaRepository.save(criterion));
                }

                return persistedResults;
    }

        private boolean hasSameRule(
                        EligibilityCriteria existing,
                        EligibilityCriteria parsed) {
                return normalize(existing.getCriterionType()).equals(normalize(parsed.getCriterionType()))
                                && normalize(existing.getOperator()).equals(normalize(parsed.getOperator()))
                                && normalize(existing.getValue()).equals(normalize(parsed.getValue()));
        }

        private String normalize(String value) {
                return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
        }

    private EligibilityCriteria createCriterion(
            Scholarship scholarship,
            String criterionType,
            String operator,
            String value,
            String value2,
            String status,
            String evidence) {

        EligibilityCriteria criterion =
                new EligibilityCriteria();

        criterion.setScholarship(scholarship);
        criterion.setCriterionType(criterionType);
        criterion.setOperator(operator);
        criterion.setValue(value);
        criterion.setValue2(value2);
        criterion.setStatus(status);
        criterion.setEvidence(evidence);

        return criterion;
    }

    private boolean containsAny(
            String text,
            String... values) {

        for (String value : values) {

            if (containsIgnoreCase(text, value)) {
                return true;
            }
        }

        return false;
    }

    private boolean containsIgnoreCase(
            String text,
            String value) {

        return text.toLowerCase()
                .contains(value.toLowerCase());
    }
}