package com.scholarmate.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record RecordDecisionsRequest(
        @NotEmpty(message = "Decisions list cannot be empty")
        List<FieldDecisionItem> decisions
) {}
