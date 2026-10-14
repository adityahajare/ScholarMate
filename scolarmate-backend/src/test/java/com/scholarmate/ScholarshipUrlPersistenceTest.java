package com.scholarmate;

import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.FeatureStatusValue;
import com.scholarmate.entity.FeatureStatus;
import com.scholarmate.entity.User;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.repository.ScholarshipRepository;
import com.scholarmate.repository.UserProfileRepository;
import com.scholarmate.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "scholarmate.scraper.schedule.enabled=false")
@Transactional
class ScholarshipUrlPersistenceTest {

    @Autowired
    private ScholarshipRepository scholarshipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsScholarshipUrlsLongerThanDefaultVarcharLength() {
        String longUrl = "https://example.org/" + "a".repeat(300);
        Scholarship scholarship = Scholarship.builder()
                .name("URL length persistence test")
                .sourceUrl(longUrl)
                .applicationUrl(longUrl)
                .specificationUrl(longUrl)
                .faqUrl(longUrl)
                .features(Map.of("minimum_cgpa", new FeatureStatusValue(FeatureStatus.KNOWN, 7.5)))
                .active(true)
                .build();

        Scholarship saved = scholarshipRepository.saveAndFlush(scholarship);
            entityManager.clear();
            Scholarship reloaded = scholarshipRepository.findById(saved.getId()).orElseThrow();

            assertEquals(longUrl, reloaded.getSourceUrl());
            assertEquals(longUrl, reloaded.getApplicationUrl());
            assertEquals(longUrl, reloaded.getSpecificationUrl());
            assertEquals(longUrl, reloaded.getFaqUrl());
            assertEquals(new FeatureStatusValue(FeatureStatus.KNOWN, 7.5), reloaded.getFeatures().get("minimum_cgpa"));
            }

            @Test
            void persistsStatusAwareStudentFeaturesAsJsonb() {
            User user = new User();
            user.setName("Feature persistence test");
            user.setEmail("feature-persistence@example.test");
            user.setPassword("test-password-hash");
            user.setRole("STUDENT");
            User savedUser = userRepository.saveAndFlush(user);

            UserProfile profile = UserProfile.builder()
                .user(savedUser)
                .educationStage("SCHOOL")
                .features(Map.of(
                    "branch", new FeatureStatusValue(FeatureStatus.NOT_APPLICABLE, null),
                    "class_number", new FeatureStatusValue(FeatureStatus.KNOWN, 10),
                    "current_score", new FeatureStatusValue(FeatureStatus.UNKNOWN, null)))
                .behaviors(Map.of(
                        "views_last_7_days", new FeatureStatusValue(FeatureStatus.KNOWN, 0)))
                .build();
            userProfileRepository.saveAndFlush(profile);
            entityManager.clear();

            UserProfile reloaded = userProfileRepository.findByUserId(savedUser.getId()).orElseThrow();

            assertEquals("SCHOOL", reloaded.getEducationStage());
            assertEquals(new FeatureStatusValue(FeatureStatus.NOT_APPLICABLE, null), reloaded.getFeatures().get("branch"));
            assertEquals(new FeatureStatusValue(FeatureStatus.KNOWN, 10), reloaded.getFeatures().get("class_number"));
            assertEquals(new FeatureStatusValue(FeatureStatus.UNKNOWN, null), reloaded.getFeatures().get("current_score"));
            assertEquals(new FeatureStatusValue(FeatureStatus.KNOWN, 0), reloaded.getBehaviors().get("views_last_7_days"));
    }
}
