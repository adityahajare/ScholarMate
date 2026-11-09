package com.scholarmate.discovery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdaptiveQuestionDto {
    private String fieldKey;
    private String label;
    private String section;
    private String dataType;
    private boolean required;
    private String validationRule;
    private String reason;
    private double priorityScore;
    private String displayCondition;
    @Builder.Default
    private List<OptionDto> options = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDto {
        private String value;
        private String label;
    }
}
