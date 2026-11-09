package com.scholarmate.discovery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateAnalysisResult {
    @Builder.Default
    private List<String> candidateScholarshipKeys = new ArrayList<>();
    @Builder.Default
    private List<String> missingFieldKeys = new ArrayList<>();
    @Builder.Default
    private Map<String, List<String>> requirementFieldMap = new HashMap<>();
    @Builder.Default
    private Map<String, String> scholarshipNames = new HashMap<>();
    @Builder.Default
    private Set<String> knownFieldKeys = new HashSet<>();
}
