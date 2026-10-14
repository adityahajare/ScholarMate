package com.scholarmate.dto;

import java.util.List;

public record ProfileDocumentReviewResponse(
        String inputMethod,
        String extractedText,
        ExtractedStudentProfile suggestedProfile,
        List<DocumentScholarshipComparison> comparisons,
        List<String> warnings) {
}