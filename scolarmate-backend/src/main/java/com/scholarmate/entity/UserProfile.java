package com.scholarmate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "student_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    private Integer age;

    private String lifeStage;

    private String occupation;

    private String institutionType;

    private String course;

    private String college;

    private Double cgpa;

    private Double familyIncome;

    private String category;

    private String state;

    private Integer yearOfStudy;

    private String gender;

    private String domicileState;

    private String educationStage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ml_features", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, FeatureStatusValue> features = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ml_behaviors", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, FeatureStatusValue> behaviors = new HashMap<>();

    /**
     * Stores the complete frontend profile form so fields that are more
     * detailed than the ML profile remain available after a new login/device.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "profile_data", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> profileData = new HashMap<>();

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public int calculateCompletionPercentage() {
        // Preserve the original public entity contract: these ten core fields
        // are the baseline completion score. The API service exposes the more
        // detailed 14-field checklist for the universal profile builder.
        int total = 10;
        int completed = 0;

        if (isFilled(fullName)) completed++;
        if (isFilled(lifeStage)) completed++;
        if (isFilled(institutionType)) completed++;
        if (isFilled(course)) completed++;
        if (isFilled(college)) completed++;
        if (cgpa != null) completed++;
        if (familyIncome != null) completed++;
        if (isFilled(category)) completed++;
        if (isFilled(state)) completed++;
        if (isFilled(gender)) completed++;
        if (completed > total) completed = total;

        return (completed * 100) / total;
    }

    private boolean isFilled(String value) {
        return value != null && !value.isBlank();
    }
}
