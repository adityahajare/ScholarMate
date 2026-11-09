package com.scholarmate.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentSessionResponse {
    private String sessionId;
    private Long scholarshipId;
    private String scholarshipName;
    private String portalName;
    private String portalUrl;
    private String status;
    private int totalFields;
    private int filledFields;
    private int acceptedFields;
    private int rejectedFields;
    private int missingFields;
    private int completionPercentage;
    private List<FieldAutofillSuggestion> suggestions;
    private List<String> missingFieldLabels;
    private LocalDateTime updatedAt;
}
