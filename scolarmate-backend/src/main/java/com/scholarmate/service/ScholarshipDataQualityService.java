package com.scholarmate.service;

import com.scholarmate.dto.ScholarshipDataQualityReport;
import com.scholarmate.entity.EligibilityCriteria;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.repository.EligibilityCriteriaRepository;
import com.scholarmate.repository.ScholarshipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ScholarshipDataQualityService {

    private final ScholarshipRepository scholarshipRepository;
    private final EligibilityCriteriaRepository criteriaRepository;

    public ScholarshipDataQualityService(
            ScholarshipRepository scholarshipRepository,
            EligibilityCriteriaRepository criteriaRepository) {
        this.scholarshipRepository = scholarshipRepository;
        this.criteriaRepository = criteriaRepository;
    }

    @Transactional(readOnly = true)
    public ScholarshipDataQualityReport createReport() {
        List<Scholarship> scholarships = scholarshipRepository.findAll();
        List<EligibilityCriteria> criteria = criteriaRepository.findAll();

        Map<Long, Long> criteriaByScholarship = criteria.stream()
                .filter(criterion -> criterion.getScholarship() != null)
                .collect(Collectors.groupingBy(
                        criterion -> criterion.getScholarship().getId(),
                        Collectors.counting()
                ));
        Map<String, Long> names = frequencyMap(scholarships, scholarship -> scholarship.getName());
        Map<String, Long> applicationUrls = frequencyMap(scholarships, scholarship -> scholarship.getApplicationUrl());
        Map<CriterionKey, Long> duplicateCriterionKeys = criteria.stream()
                .filter(criterion -> criterion.getScholarship() != null)
                .filter(criterion -> hasText(criterion.getCriterionType()))
                .collect(Collectors.groupingBy(
                        criterion -> new CriterionKey(
                                criterion.getScholarship().getId(),
                                normalize(criterion.getCriterionType()),
                                normalize(criterion.getOperator()),
                                normalize(criterion.getValue()),
                                normalize(criterion.getValue2())
                        ),
                        Collectors.counting()
                ));

        long totalCriteria = criteriaByScholarship.values().stream()
                .mapToLong(value -> value.longValue())
                .sum();
        int withoutCriteria = (int) scholarships.stream()
                .filter(scholarship -> !criteriaByScholarship.containsKey(scholarship.getId()))
                .count();
        int withUnknownCriteria = (int) criteria.stream()
                .filter(criterion -> "UNKNOWN".equalsIgnoreCase(criterion.getStatus())
                        || "UNKNOWN".equalsIgnoreCase(criterion.getCriterionType()))
                .map(criterion -> criterion.getScholarship() == null
                        ? null
                        : criterion.getScholarship().getId())
                .filter(java.util.Objects::nonNull)
                .distinct()
                .count();
        int maximumCriteria = criteriaByScholarship.values().stream()
                .mapToInt(value -> value.intValue())
                .max()
                .orElse(0);
        double averageCriteria = scholarships.isEmpty()
                ? 0.0
                : (double) totalCriteria / scholarships.size();

        return new ScholarshipDataQualityReport(
                scholarships.size(),
                (int) scholarships.stream().filter(s -> Boolean.TRUE.equals(s.getActive())).count(),
                duplicateGroups(names),
                duplicateGroups(applicationUrls),
                (int) scholarships.stream().filter(s -> !hasText(s.getProvider())).count(),
                (int) scholarships.stream().filter(s -> !hasText(s.getApplicationUrl())).count(),
                (int) scholarships.stream().filter(s -> !hasText(s.getDescription())).count(),
                scholarships.size() - withoutCriteria,
                withUnknownCriteria,
                withoutCriteria,
                maximumCriteria,
                averageCriteria,
                duplicateGroups(duplicateCriterionKeys),
                "UNKNOWN"
        );
    }

    private Map<String, Long> frequencyMap(
            List<Scholarship> scholarships,
            Function<Scholarship, String> valueExtractor) {
        Map<String, Long> frequencies = new HashMap<>();
        for (Scholarship scholarship : scholarships) {
            String value = valueExtractor.apply(scholarship);
            if (hasText(value)) {
                frequencies.merge(normalize(value), 1L, (left, right) -> left + right);
            }
        }
        return frequencies;
    }

        private int duplicateGroups(Map<?, Long> frequencies) {
        return (int) frequencies.values().stream().filter(count -> count > 1).count();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
                return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

        private record CriterionKey(
                        Long scholarshipId,
                        String type,
                        String operator,
                        String value,
                        String value2) {
        }
}
