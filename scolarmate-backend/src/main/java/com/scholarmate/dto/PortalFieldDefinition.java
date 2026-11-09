package com.scholarmate.dto;

import java.util.List;

public record PortalFieldDefinition(
        String fieldKey,
        String label,
        String fieldType,
        List<String> options,
        boolean required
) {
    public PortalFieldDefinition(String fieldKey, String label, String fieldType) {
        this(fieldKey, label, fieldType, null, false);
    }
}
