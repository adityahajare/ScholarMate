package com.scholarmate.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * Payload used when creating or updating a profile draft.
 * All fields are optional because the draft can be built step by step.
 */
public class ProfileDraftRequest {

    /**
     * Arbitrary JSON map containing the draft data. The front‑end will serialise
     * its step‑wise model into this map.
     */
    @NotNull(message = "Draft data must be provided")
    private Map<String, Object> data;

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}

