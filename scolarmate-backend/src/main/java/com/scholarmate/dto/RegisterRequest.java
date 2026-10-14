package com.scholarmate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

import com.scholarmate.entity.FeatureStatusValue;
import java.util.HashMap;
import java.util.Map;

@Data
public class RegisterRequest {

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String course;

    private String college;

    private String category;

    private String state;

    private String gender;

    private Integer yearOfStudy;

    private Double cgpa;

    private Double familyIncome;

    @Min(3)
    @Max(120)
    private Integer age;

    private String lifeStage;

    private String occupation;

    private String institutionType;

    private String educationStage;

    private Map<String, FeatureStatusValue> features = new HashMap<>();

    private Map<String, FeatureStatusValue> behaviors = new HashMap<>();
}
