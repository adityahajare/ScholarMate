package com.scholarmate.dto;

public record FieldDecisionItem(
        String fieldKey,
        String decision,
        String customValue
) {
    public FieldDecisionItem(String fieldKey, String decision) {
        this(fieldKey, decision, null);
    }
}
