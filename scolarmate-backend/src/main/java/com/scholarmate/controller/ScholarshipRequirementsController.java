package com.scholarmate.controller;

import com.scholarmate.dto.ScholarshipRequirementsResponse;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.repository.EligibilityCriteriaRepository;
import com.scholarmate.repository.RequiredDocumentRepository;
import com.scholarmate.repository.ScholarshipEvidenceRepository;
import com.scholarmate.repository.ScholarshipRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile/requirements")
public class ScholarshipRequirementsController {

    private final ScholarshipRepository scholarshipRepository;
    private final EligibilityCriteriaRepository criteriaRepository;
    private final RequiredDocumentRepository requiredDocumentRepository;
    private final ScholarshipEvidenceRepository evidenceRepository;

    public ScholarshipRequirementsController(
            ScholarshipRepository scholarshipRepository,
            EligibilityCriteriaRepository criteriaRepository,
            RequiredDocumentRepository requiredDocumentRepository,
            ScholarshipEvidenceRepository evidenceRepository) {
        this.scholarshipRepository = scholarshipRepository;
        this.criteriaRepository = criteriaRepository;
        this.requiredDocumentRepository = requiredDocumentRepository;
        this.evidenceRepository = evidenceRepository;
    }

    @GetMapping("/{scholarshipId}")
    public ResponseEntity<ScholarshipRequirementsResponse> getRequirements(@PathVariable Long scholarshipId) {
        Scholarship scholarship = scholarshipRepository.findById(scholarshipId)
                .orElseThrow(() -> new RuntimeException("Scholarship not found"));

        var criteria = criteriaRepository.findByScholarshipId(scholarshipId).stream()
                .map(item -> new ScholarshipRequirementsResponse.Criterion(
                        item.getId(), item.getCriterionType(), item.getOperator(), item.getValue(),
                        item.getValue2(), item.getStatus(), item.getEvidence()))
                .toList();
        var documents = requiredDocumentRepository.findByScholarshipId(scholarshipId).stream()
                .map(item -> new ScholarshipRequirementsResponse.RequiredDocument(
                        item.getId(), item.getDocumentType(), item.getRequirementType(),
                        item.getCondition(), item.getEvidence()))
                .toList();
        var evidence = evidenceRepository.findByScholarshipId(scholarshipId).stream()
                .map(item -> new ScholarshipRequirementsResponse.Evidence(
                        item.getId(), item.getEvidenceType(), item.getTitle(), item.getSourceUrl(),
                        item.getExtractionMethod(), item.getConfidence(), item.getRetrievedAt()))
                .toList();

        return ResponseEntity.ok(new ScholarshipRequirementsResponse(
                scholarship.getId(), scholarship.getName(), criteria, documents, evidence));
    }
}
