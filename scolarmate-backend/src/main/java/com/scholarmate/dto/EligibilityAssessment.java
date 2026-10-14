package com.scholarmate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibilityAssessment {

    private Long scholarshipId;
    private String status;

    @Builder.Default
    private List<String> matchedCriteria = new ArrayList<>();

    @Builder.Default
    private List<String> failedCriteria = new ArrayList<>();

    @Builder.Default
    private List<String> unknownCriteria = new ArrayList<>();

    @Builder.Default
    private List<String> evidence = new ArrayList<>();
}
