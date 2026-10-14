package com.scholarmate.service;

import com.scholarmate.dto.EligibilityAssessment;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.EligibilityCriteria;
import com.scholarmate.entity.UserProfile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EligibilityEvaluationServiceTest {

    private final EligibilityEvaluationService service = new EligibilityEvaluationService();

    @Test
    void shouldReportEligibleWhenStudentMeetsTheScholarshipRequirements() {
        UserProfile profile = new UserProfile();
        profile.setCgpa(8.2);
        profile.setFamilyIncome(250000.0);
        profile.setCourse("B.Tech");
        profile.setState("Maharashtra");
        profile.setCategory("OBC");
        profile.setYearOfStudy(3);

        Scholarship scholarship = Scholarship.builder()
                .id(1L)
                .minimumCgpa(7.0)
                .maximumFamilyIncome(800000.0)
                .course("B.Tech")
                .state("Maharashtra")
                .category("OBC")
                .minimumYearOfStudy(1)
                .maximumYearOfStudy(4)
                .build();

        EligibilityAssessment assessment = service.evaluate(profile, scholarship, List.of());

        assertEquals("ELIGIBLE", assessment.getStatus());
        assertFalse(assessment.getMatchedCriteria().isEmpty());
    }

    @Test
    void shouldReportNotEligibleWhenStudentFailsARequiredRule() {
        UserProfile profile = new UserProfile();
        profile.setCgpa(5.0);
        profile.setFamilyIncome(250000.0);
        profile.setCourse("B.Tech");
        profile.setState("Maharashtra");
        profile.setCategory("OBC");
        profile.setYearOfStudy(3);

        Scholarship scholarship = Scholarship.builder()
                .id(2L)
                .minimumCgpa(7.0)
                .maximumFamilyIncome(800000.0)
                .course("B.Tech")
                .state("Maharashtra")
                .build();

        EligibilityAssessment assessment = service.evaluate(profile, scholarship, List.of());

        assertEquals("NOT_ELIGIBLE", assessment.getStatus());
        assertFalse(assessment.getFailedCriteria().isEmpty());
    }

    @Test
    void shouldReportUnknownWhenCriticalInformationIsMissing() {
        UserProfile profile = new UserProfile();
        profile.setCourse("B.Tech");
        profile.setState("Maharashtra");
        profile.setCategory("OBC");
        profile.setYearOfStudy(3);
        profile.setFamilyIncome(null);
        profile.setCgpa(null);

        Scholarship scholarship = Scholarship.builder()
                .id(3L)
                .minimumCgpa(7.0)
                .maximumFamilyIncome(800000.0)
                .course("B.Tech")
                .state("Maharashtra")
                .build();

        EligibilityAssessment assessment = service.evaluate(profile, scholarship, List.of());

        assertEquals("UNKNOWN", assessment.getStatus());
        assertFalse(assessment.getUnknownCriteria().isEmpty());
        assertTrue(assessment.getEvidence().stream().anyMatch(item -> item.contains("missing")) || assessment.getEvidence().stream().anyMatch(item -> item.contains("Missing")));
    }

    @Test
    void shouldKeepPlaceholderEligibilityUnknown() {
        UserProfile profile = new UserProfile();
        profile.setCgpa(10.0);
        Scholarship scholarship = Scholarship.builder().id(4L).build();
        EligibilityCriteria placeholder = EligibilityCriteria.builder()
                .criterionType("UNKNOWN")
                .operator("UNKNOWN")
                .status("UNKNOWN")
                .evidence("No eligibility criteria could be extracted.")
                .build();

        EligibilityAssessment assessment = service.evaluate(profile, scholarship, List.of(placeholder));

        assertEquals("UNKNOWN", assessment.getStatus());
        assertTrue(assessment.getUnknownCriteria().contains("UNKNOWN"));
    }

    @Test
    void shouldNotCompareFamilyChildLimitToYearOfStudy() {
        UserProfile profile = new UserProfile();
        profile.setYearOfStudy(4);
        Scholarship scholarship = Scholarship.builder().id(5L).build();
        EligibilityCriteria criterion = EligibilityCriteria.builder()
                .criterionType("MAX_CHILDREN_PER_FAMILY")
                .operator("LESS_THAN_OR_EQUAL")
                .value("2")
                .build();

        EligibilityAssessment assessment = service.evaluate(profile, scholarship, List.of(criterion));

        assertEquals("UNKNOWN", assessment.getStatus());
        assertTrue(assessment.getUnknownCriteria().contains("MAX_CHILDREN_PER_FAMILY"));
    }
}
