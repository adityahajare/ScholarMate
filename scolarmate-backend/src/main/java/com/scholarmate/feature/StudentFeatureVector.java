package com.scholarmate.feature;

import lombok.*;

/**
 * Simple container that groups a student’s structured profile
 * and optional behavioural signals. This is the object passed
 * to the ML service for encoding.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentFeatureVector {
    private StudentProfileFeatures profile;
    private UserBehaviourFeatures behaviour;
}

