package com.scholarmate.controller;

import com.scholarmate.entity.EligibilityCriteria;
import com.scholarmate.entity.RequiredDocument;
import com.scholarmate.entity.ScholarshipEvidence;

import com.scholarmate.service.ScholarshipEligibilityParser;
import com.scholarmate.service.ScholarshipEvidenceService;
import com.scholarmate.service.ScholarshipDocumentParser;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parser")
public class ScholarshipParsingController {

    private final ScholarshipEvidenceService evidenceService;

    private final ScholarshipEligibilityParser eligibilityParser;

    private final ScholarshipDocumentParser documentParser;

    public ScholarshipParsingController(
            ScholarshipEvidenceService evidenceService,
            ScholarshipEligibilityParser eligibilityParser,
            ScholarshipDocumentParser documentParser) {

        this.evidenceService = evidenceService;
        this.eligibilityParser = eligibilityParser;
        this.documentParser = documentParser;
    }

    @PostMapping("/eligibility/{scholarshipId}")
    public List<EligibilityCriteria> parseEligibility(
            @PathVariable Long scholarshipId) {

        ScholarshipEvidence evidence =
                getEvidence(scholarshipId);

        return eligibilityParser.parseAndSave(
                scholarshipId,
                evidence.getExtractedText()
        );
    }

    @PostMapping("/documents/{scholarshipId}")
    public List<RequiredDocument> parseDocuments(
            @PathVariable Long scholarshipId) {

        ScholarshipEvidence evidence =
                getEvidence(scholarshipId);

        return documentParser.parseAndSave(
                scholarshipId,
                evidence.getExtractedText()
        );
    }

    @PostMapping("/all/{scholarshipId}")
    public Map<String, Object> parseAll(
            @PathVariable Long scholarshipId) {

        ScholarshipEvidence evidence =
                getEvidence(scholarshipId);

        List<EligibilityCriteria> eligibility =
                eligibilityParser.parseAndSave(
                        scholarshipId,
                        evidence.getExtractedText()
                );

        List<RequiredDocument> documents =
                documentParser.parseAndSave(
                        scholarshipId,
                        evidence.getExtractedText()
                );

        Map<String, Object> result =
                new HashMap<>();

        result.put(
                "eligibility",
                eligibility
        );

        result.put(
                "documents",
                documents
        );

        return result;
    }

    private ScholarshipEvidence getEvidence(
            Long scholarshipId) {

        List<ScholarshipEvidence> evidence =
                evidenceService.getByScholarship(
                        scholarshipId
                );

        if (evidence.isEmpty()) {

            throw new RuntimeException(
                    "No evidence found for scholarship"
            );
        }

        return evidence.stream()
                .filter(e ->
                        e.getExtractionMethod() != null
                                &&
                                e.getExtractionMethod()
                                        .toLowerCase()
                                        .contains("pdf")
                )
                .findFirst()
                .orElse(evidence.get(0));
    }
}