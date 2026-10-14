package com.scholarmate.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.scholarmate.entity.FeatureStatusValue;

public record MlRecommendationRequest(
        Student student,
        List<Candidate> scholarships) {

    public record Student(
            String course,
            String college,
            Double cgpa,
            Double familyIncome,
            String category,
            String state,
            String domicileState,
            Integer yearOfStudy,
            String gender,
            String educationStage,
            Map<String, FeatureStatusValue> features,
            Map<String, FeatureStatusValue> behaviors) {
    }

    public record Candidate(
            Long id,
            String name,
            String description,
            String provider,
            String course,
            String category,
            String eligibleCategories,
            String state,
            Double minimumCgpa,
            Double maximumFamilyIncome,
            Integer minimumYearOfStudy,
            Integer maximumYearOfStudy,
            Double amount,
            LocalDate applicationDeadline,
            String applicationUrl,
            String sourceUrl,
            Map<String, FeatureStatusValue> features) {
    }
}