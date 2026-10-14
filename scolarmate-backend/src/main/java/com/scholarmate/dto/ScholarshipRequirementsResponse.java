package com.scholarmate.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ScholarshipRequirementsResponse(
        Long scholarshipId,
        String scholarshipName,
        List<Criterion> criteria,
        List<RequiredDocument> requiredDocuments,
        List<Evidence> evidence) {

    public record Criterion(
            Long id,
            String criterionType,
            String operator,
            String value,
            String value2,
            String status,
            String evidence) { }

    public record RequiredDocument(
            Long id,
            String documentType,
            String requirementType,
            String condition,
            String evidence) { }

    public record Evidence(
            Long id,
            String evidenceType,
            String title,
            String sourceUrl,
            String extractionMethod,
            Double confidence,
            LocalDateTime retrievedAt) { }
}
