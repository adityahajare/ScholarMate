package com.scholarmate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileCompletionResponse {
    private int completionPercentage;
    private int completedFields;
    private int totalFields;
    private List<String> missingFields;
}
