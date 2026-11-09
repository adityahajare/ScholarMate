package com.scholarmate.dto;

import jakarta.validation.constraints.NotBlank;

public record InitAgentSessionRequest(
        Long scholarshipId,

        @NotBlank(message = "Portal name is required (e.g., National Scholarship Portal, MahaDBT)")
        String portalName,

        String portalUrl
) {}
