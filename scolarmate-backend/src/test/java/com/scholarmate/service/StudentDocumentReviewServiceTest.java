package com.scholarmate.service;

import com.scholarmate.entity.Scholarship;
import com.scholarmate.repository.EligibilityCriteriaRepository;
import com.scholarmate.repository.ScholarshipRepository;
import com.scholarmate.scraper.PdfScholarshipExtractor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StudentDocumentReviewServiceTest {

    @Test
    void pastedDocumentIsParsedAndComparedWithoutSavingAProfile() {
        PdfScholarshipExtractor extractor = mock(PdfScholarshipExtractor.class);
        ScholarshipRepository scholarships = mock(ScholarshipRepository.class);
        EligibilityCriteriaRepository criteria = mock(EligibilityCriteriaRepository.class);
        Scholarship scholarship = Scholarship.builder()
                .id(7L)
                .name("Physics Merit Award")
                .active(true)
                .minimumCgpa(7.0)
                .maximumFamilyIncome(200000.0)
                .course("Physics")
                .build();
        when(scholarships.findAll()).thenReturn(List.of(scholarship));
        when(criteria.findByScholarshipId(7L)).thenReturn(List.of());

        StudentDocumentReviewService service = new StudentDocumentReviewService(
                extractor,
                new StudentProfileDocumentParser(),
                scholarships,
                criteria,
                new EligibilityEvaluationService()
        );

        var response = service.reviewText("""
                Name: Priya Kumar
                Course: BSc Physics
                College: Example University
                CGPA: 8.5
                Annual Family Income: INR 150,000
                Category: OBC
                State: Maharashtra
                Year of Study: 2nd
                """);

        assertEquals("PASTED_TEXT", response.inputMethod());
        assertEquals(8.5, response.suggestedProfile().cgpa());
        assertEquals(150000.0, response.suggestedProfile().familyIncome());
        assertEquals("ELIGIBLE", response.comparisons().getFirst().status());
    }
}