package com.scholarmate.service;

import com.scholarmate.entity.EligibilityCriteria;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.dto.ScholarshipDataQualityReport;
import com.scholarmate.repository.EligibilityCriteriaRepository;
import com.scholarmate.repository.ScholarshipRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScholarshipDataQualityServiceTest {

    @Test
    void reportsDuplicateMissingAndSparseEligibilityData() {
        Scholarship first = Scholarship.builder()
                .id(1L)
                .name("  Award   One ")
                .description("Details")
                .applicationUrl(null)
                .active(true)
                .build();
        Scholarship duplicateName = Scholarship.builder()
                .id(2L)
                .name("award one")
                .provider("Provider")
                .description("Details")
                .applicationUrl("https://example.org/apply")
                .active(true)
                .build();
        Scholarship duplicateUrl = Scholarship.builder()
                .id(3L)
                .name("Award Two")
                .provider("Provider")
                .applicationUrl("https://example.org/apply")
                .active(true)
                .build();

        EligibilityCriteria ruleOne = EligibilityCriteria.builder()
                .scholarship(first)
                .criterionType("MINIMUM_CGPA")
                .build();
        EligibilityCriteria repeatedRule = EligibilityCriteria.builder()
                .scholarship(first)
                .criterionType("minimum_cgpa")
                .build();

        ScholarshipRepository scholarshipRepository = mock(ScholarshipRepository.class);
        EligibilityCriteriaRepository criteriaRepository = mock(EligibilityCriteriaRepository.class);
        when(scholarshipRepository.findAll()).thenReturn(List.of(first, duplicateName, duplicateUrl));
        when(criteriaRepository.findAll()).thenReturn(List.of(ruleOne, repeatedRule));

        ScholarshipDataQualityReport report = new ScholarshipDataQualityService(
                scholarshipRepository,
                criteriaRepository
        ).createReport();

        assertEquals(3, report.totalScholarships());
        assertEquals(1, report.duplicateNormalizedNameGroups());
        assertEquals(1, report.duplicateApplicationUrlGroups());
        assertEquals(1, report.missingProviders());
        assertEquals(1, report.missingApplicationUrls());
        assertEquals(1, report.missingDescriptions());
        assertEquals(1, report.scholarshipsWithCriteria());
        assertEquals(0, report.scholarshipsWithUnknownCriteria());
        assertEquals(2, report.scholarshipsWithoutCriteria());
        assertEquals(2, report.maximumCriteriaPerScholarship());
        assertEquals(1, report.duplicateCriterionGroups());
        assertEquals("UNKNOWN", report.defaultEligibilityStatusWhenRulesAreMissing());
    }
}