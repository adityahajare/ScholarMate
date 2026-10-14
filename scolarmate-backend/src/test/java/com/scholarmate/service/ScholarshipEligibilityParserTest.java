package com.scholarmate.service;

import com.scholarmate.entity.EligibilityCriteria;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.repository.EligibilityCriteriaRepository;
import com.scholarmate.repository.ScholarshipRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScholarshipEligibilityParserTest {

    @Test
    void reparsingSameEvidenceUpdatesRatherThanDuplicatesCriteria() {
        EligibilityCriteriaRepository criteriaRepository = mock(EligibilityCriteriaRepository.class);
        ScholarshipRepository scholarshipRepository = mock(ScholarshipRepository.class);
        ScholarshipSectionExtractor sectionExtractor = mock(ScholarshipSectionExtractor.class);
        EligibilityCriteriaDefaultsService defaultsService = mock(EligibilityCriteriaDefaultsService.class);
        Scholarship scholarship = Scholarship.builder().id(12L).name("Merit Award").build();
        List<EligibilityCriteria> storedCriteria = new ArrayList<>();

        when(scholarshipRepository.findById(12L)).thenReturn(Optional.of(scholarship));
        when(sectionExtractor.extractSections("eligibility evidence"))
                .thenReturn(Map.of("ELIGIBILITY", "Minimum 60% marks"));
        when(criteriaRepository.findByScholarshipId(12L))
                .thenAnswer(invocation -> new ArrayList<>(storedCriteria));
        when(criteriaRepository.save(any(EligibilityCriteria.class))).thenAnswer(invocation -> {
            EligibilityCriteria criterion = invocation.getArgument(0);
            if (criterion.getId() == null) {
                criterion.setId(1L);
            }
            if (storedCriteria.stream().noneMatch(existing -> existing.getId().equals(criterion.getId()))) {
                storedCriteria.add(criterion);
            }
            return criterion;
        });

        ScholarshipEligibilityParser parser = new ScholarshipEligibilityParser(
                criteriaRepository,
                scholarshipRepository,
                sectionExtractor,
                defaultsService
        );

        parser.parseAndSave(12L, "eligibility evidence");
        parser.parseAndSave(12L, "eligibility evidence");

        assertEquals(1, storedCriteria.size());
        assertEquals("MINIMUM_MARKS", storedCriteria.get(0).getCriterionType());
    }
}