package com.scholarmate.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record InspectPortalFieldsRequest(
        @NotEmpty(message = "Portal fields list cannot be empty")
        List<PortalFieldDefinition> fields
) {}
