package com.scholarmate.dto;

public record ScholarshipDataQualityReport(
        int totalScholarships,
        int activeScholarships,
        int duplicateNormalizedNameGroups,
        int duplicateApplicationUrlGroups,
        int missingProviders,
        int missingApplicationUrls,
        int missingDescriptions,
        int scholarshipsWithCriteria,
        int scholarshipsWithUnknownCriteria,
        int scholarshipsWithoutCriteria,
        int maximumCriteriaPerScholarship,
        double averageCriteriaPerScholarship,
        int duplicateCriterionGroups,
        String defaultEligibilityStatusWhenRulesAreMissing) {
}