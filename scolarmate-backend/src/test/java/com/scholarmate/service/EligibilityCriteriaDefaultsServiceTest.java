package com.scholarmate.service;

import com.scholarmate.entity.EligibilityCriteria;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.repository.EligibilityCriteriaRepository;
import com.scholarmate.repository.ScholarshipRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EligibilityCriteriaDefaultsServiceTest {

    @Test
    void backfillsOneUnknownPlaceholderAndIsIdempotent() {
        Scholarship scholarship = Scholarship.builder().id(4L).name("Merit Award").build();
        List<EligibilityCriteria> stored = new ArrayList<>();
        ScholarshipRepository scholarshipRepository = mock(ScholarshipRepository.class);
        EligibilityCriteriaRepository criteriaRepository = mock(EligibilityCriteriaRepository.class);
        when(scholarshipRepository.findAll()).thenReturn(List.of(scholarship));
        when(criteriaRepository.findAll()).thenAnswer(invocation -> new ArrayList<>(stored));
        when(criteriaRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<EligibilityCriteria> rows = invocation.getArgument(0);
            rows.forEach(stored::add);
            return stored;
        });

        EligibilityCriteriaDefaultsService service = new EligibilityCriteriaDefaultsService(
                scholarshipRepository,
                criteriaRepository
        );

        assertEquals(1, service.ensurePlaceholdersForMissingScholarships());
        assertEquals(0, service.ensurePlaceholdersForMissingScholarships());
        assertEquals(1, stored.size());
        assertEquals("UNKNOWN", stored.getFirst().getCriterionType());
        assertEquals("UNKNOWN", stored.getFirst().getStatus());
        assertNull(stored.getFirst().getValue());
    }
}