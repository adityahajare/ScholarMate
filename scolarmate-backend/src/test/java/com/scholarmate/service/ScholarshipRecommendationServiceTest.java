package com.scholarmate.service;

import com.scholarmate.dto.RecommendationResponse;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.UserProfile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ScholarshipRecommendationServiceTest {

    private final ScholarshipRecommendationService service = new ScholarshipRecommendationService();

    @Test
    void shouldReturnEligibleAndUnknownBucketsForUserRecommendations() {
        UserProfile profile = new UserProfile();
        profile.setCourse("B.Tech");
        profile.setState("Maharashtra");
        profile.setCategory("OBC");
        profile.setCgpa(8.5);
        profile.setFamilyIncome(250000.0);
        profile.setYearOfStudy(3);

        Scholarship eligibleScholarship = Scholarship.builder()
                .id(101L)
                .name("Merit Scholarship")
                .course("B.Tech")
                .state("Maharashtra")
                .category("OBC")
                .minimumCgpa(7.0)
                .maximumFamilyIncome(800000.0)
                .minimumYearOfStudy(1)
                .maximumYearOfStudy(4)
                .build();

        Scholarship unknownScholarship = Scholarship.builder()
                .id(102L)
                .name("Needs Review Scholarship")
                .course("B.Tech")
                .state("Maharashtra")
                .minimumCgpa(7.0)
                .maximumFamilyIncome(800000.0)
                .minimumYearOfStudy(1)
                .maximumYearOfStudy(4)
                .build();

        RecommendationResponse response = service.buildRecommendation(profile, List.of(eligibleScholarship, unknownScholarship));

        assertNotNull(response);
        assertEquals(1, response.getEligible().size());
        assertEquals(1, response.getUnknown().size());
    }
}
