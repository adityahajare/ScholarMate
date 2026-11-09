package com.scholarmate.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldAutofillSuggestion {
    private String fieldKey;
    private String label;
    private String suggestedValue;
    private double confidence;
    private String source;
    private String explanation;
    private String status;
    private String userDecision;
    private String userCustomValue;
}
