package com.scholarmate.controller;

import com.scholarmate.entity.ScholarshipEvidence;
import com.scholarmate.service.ScholarshipEvidenceCollector;
import com.scholarmate.service.ScholarshipEvidenceService;
import com.scholarmate.service.ScholarshipSectionExtractor;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evidence")
public class ScholarshipEvidenceController {

    private final ScholarshipEvidenceService service;

    private final ScholarshipEvidenceCollector evidenceCollector;

    private final ScholarshipSectionExtractor sectionExtractor;

    public ScholarshipEvidenceController(
            ScholarshipEvidenceService service,
            ScholarshipEvidenceCollector evidenceCollector,
            ScholarshipSectionExtractor sectionExtractor) {

        this.service = service;

        this.evidenceCollector =
                evidenceCollector;

        this.sectionExtractor =
                sectionExtractor;
    }

    @PostMapping
    public ScholarshipEvidence save(
            @RequestBody ScholarshipEvidence evidence) {

        return service.save(evidence);
    }

    @GetMapping("/scholarship/{scholarshipId}")
    public List<ScholarshipEvidence> getByScholarship(
            @PathVariable Long scholarshipId) {

        return service.getByScholarship(
                scholarshipId
        );
    }

    @GetMapping("/{id}")
    public ScholarshipEvidence getById(
            @PathVariable Long id) {

        return service.getById(id);
    }

    @PostMapping("/collect/{scholarshipId}")
    public String collectEvidence(
            @PathVariable Long scholarshipId) {

        return evidenceCollector
                .collectForScholarship(
                        scholarshipId
                );
    }

    @PostMapping("/collect-all")
    public String collectAllEvidence() {

        return evidenceCollector
                .collectForAllActiveScholarships();
    }

    @DeleteMapping("/{id}")
    public String deleteEvidence(
            @PathVariable Long id) {

        service.deleteEvidence(id);

        return "Evidence deleted successfully";
    }

    @PostMapping("/analyze/{scholarshipId}")
    public Map<String, String> analyzeEvidence(
            @PathVariable Long scholarshipId) {

        List<ScholarshipEvidence> evidence =
                service.getByScholarship(
                        scholarshipId
                );

        if (evidence.isEmpty()) {

            throw new RuntimeException(
                    "No evidence for scholarship"
            );
        }

        /*
         * Prefer PDF evidence instead of blindly taking
         * evidence.get(0).
         */

        ScholarshipEvidence evidenceToAnalyze =
                evidence.stream()
                        .filter(e ->
                                e.getExtractionMethod() != null
                                        &&
                                        e.getExtractionMethod()
                                                .toLowerCase()
                                                .contains("pdf")
                        )
                        .findFirst()
                        .orElse(evidence.get(0));

        return sectionExtractor.extractSections(
                evidenceToAnalyze.getExtractedText()
        );
    }
}