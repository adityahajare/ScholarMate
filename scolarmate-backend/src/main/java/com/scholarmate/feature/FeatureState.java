package com.scholarmate.feature;

public enum FeatureState {
    /**
     * The value is present and valid.
     */
    KNOWN,
    /**
     * The value is missing – the user didn't provide it.
     */
    UNKNOWN,
    /**
     * The feature does not apply to this user (e.g., "branch" for a 5th‑standard student).
     */
    NOT_APPLICABLE
}

