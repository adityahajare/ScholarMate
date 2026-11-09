package com.scholarmate.discovery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipMatchResponseDto {
    private String scholarshipKey;
    private String scholarshipName;
    private String academicYear;
    private String evaluationStatus; // POTENTIALLY_ELIGIBLE, NOT_ELIGIBLE, NEEDS_MORE_INFORMATION, PENDING_VERIFICATION
    private double matchConfidence;
    @Builder.Default
    private List<String> satisfiedConditions = new ArrayList<>();
    @Builder.Default
    private List<String> failedConditions = new ArrayList<>();
    @Builder.Default
    private List<String> missingFields = new ArrayList<>();
    @Builder.Default
    private List<String> requiredDocuments = new ArrayList<>();
    private String sourceUrl;
    private String sourceDocument;
    private String sourceSection;
    private String lastVerificationDate;
    private String eligibilitySummary;
}
