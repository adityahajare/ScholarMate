package com.scholarmate.discovery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdaptiveQuestionRankingRequestDto {
    private String studentId;
    @Builder.Default
    private List<String> candidateScholarshipIds = new ArrayList<>();
    @Builder.Default
    private List<String> knownFieldKeys = new ArrayList<>();
    @Builder.Default
    private List<String> missingFieldKeys = new ArrayList<>();
    @Builder.Default
    private List<String> alreadyAskedFieldKeys = new ArrayList<>();
    @Builder.Default
    private Map<String, List<String>> scholarshipRequirementFieldMap = new HashMap<>();
    @Builder.Default
    private Map<String, String> scholarshipNames = new HashMap<>();
}
