package com.scholarmate.dto;

import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentVaultResponse {
    private Long userId;
    private String email;
    private Map<String, Object> identity;
    private Map<String, Object> academics;
    private Map<String, Object> address;
    private Map<String, Object> family;
    private Map<String, Object> bank;
    private Map<String, Object> ocrExtracted;
    private int readinessScore;
}
