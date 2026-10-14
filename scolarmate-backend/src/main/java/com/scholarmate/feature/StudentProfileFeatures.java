package com.scholarmate.feature;

import lombok.*;

/**
 * Placeholder for a student's structured profile features.
 * In a real implementation this would contain dozens of fields such as age,
 * education stage, location, grades, etc. For now we include a minimal set
 * to satisfy compilation and to demonstrate the wrapper usage.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileFeatures {
    private Long userId;
    private Integer age;
    private String educationStage; // e.g., "UG", "PG", "SCHOOL"
    private String state;
    private String city;
    // Additional fields can be added later as needed.
}

