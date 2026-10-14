package com.scholarmate.service;

import com.scholarmate.dto.RecommendationResponse;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.UserProfile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScholarshipRecommendationService {

    private final EligibilityEvaluationService evaluator;

    public ScholarshipRecommendationService() {
        this(new EligibilityEvaluationService());
    }

    public ScholarshipRecommendationService(EligibilityEvaluationService evaluator) {
        this.evaluator = evaluator;
    }

    public RecommendationResponse buildRecommendation(UserProfile profile, List<Scholarship> scholarships) {
        RecommendationResponse response = new RecommendationResponse();

        if (profile == null || scholarships == null || scholarships.isEmpty()) {
            return response;
        }

        for (Scholarship scholarship : scholarships) {
            if (scholarship == null) {
                continue;
            }

            String title = scholarship.getName() == null || scholarship.getName().isBlank()
                    ? "Scholarship " + scholarship.getId()
                    : scholarship.getName();

            if (isIncompleteScholarshipMetadata(scholarship)) {
                response.getUnknown().add(title);
                continue;
            }

            var assessment = evaluator.evaluate(profile, scholarship, List.of());
            switch (assessment.getStatus()) {
                case "ELIGIBLE" -> response.getEligible().add(title);
                case "NOT_ELIGIBLE" -> response.getNotEligible().add(title);
                case "UNKNOWN" -> response.getUnknown().add(title);
                default -> response.getPossible().add(title);
            }
        }

        return response;
    }

    private boolean isIncompleteScholarshipMetadata(Scholarship scholarship) {
        return scholarship.getCourse() == null || scholarship.getCourse().isBlank()
                || scholarship.getState() == null || scholarship.getState().isBlank()
                || scholarship.getCategory() == null || scholarship.getCategory().isBlank()
                || scholarship.getMinimumCgpa() == null
                || scholarship.getMaximumFamilyIncome() == null
                || scholarship.getMinimumYearOfStudy() == null
                || scholarship.getMaximumYearOfStudy() == null;
    }
}
