package com.scholarmate.discovery.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryAnswersRequestDto {
    @NotEmpty(message = "Answers map cannot be empty")
    @Builder.Default
    private Map<String, Object> answers = new HashMap<>();
}
