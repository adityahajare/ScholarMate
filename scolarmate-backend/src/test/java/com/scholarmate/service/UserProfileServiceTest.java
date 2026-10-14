package com.scholarmate.service;

import com.scholarmate.entity.UserProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserProfileServiceTest {

    @Test
    void profileCompletionPercentageUsesTotalEligibleFields() {
        UserProfile profile = UserProfile.builder()
                .fullName("Amit Sharma")
                .course("B.Tech")
                .college("IIT Delhi")
                .cgpa(8.5)
                .category("General")
                .state("Delhi")
                .gender("Male")
                .build();

        assertEquals(70, profile.calculateCompletionPercentage());
    }
}
