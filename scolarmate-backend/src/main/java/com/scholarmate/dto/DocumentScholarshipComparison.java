package com.scholarmate.dto;

import java.util.List;

public record DocumentScholarshipComparison(
        Long scholarshipId,
        String scholarshipName,
        String status,
        List<String> matchedCriteria,
        List<String> unknownCriteria,
        List<String> failedCriteria,
        List<String> evidence) {
}