package com.scholarmate.feature;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Wrapper for a feature value that records whether the value is known, unknown, or not applicable.
 * @param <T> type of the underlying value (e.g., Integer, String, Double)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeatureValue<T> {
    /**
     * The actual value when {@link #state} == FeatureState.KNOWN.
     * Can be null for UNKNOWN or NOT_APPLICABLE.
     */
    private T value;

    /**
     * Indicates the provenance of the value.
     */
    private FeatureState state = FeatureState.UNKNOWN;

    /**
     * Convenience factory for a known value.
     */
    public static <T> FeatureValue<T> known(T val) {
        return new FeatureValue<>(val, FeatureState.KNOWN);
    }

    /**
     * Convenience factory for an unknown value.
     */
    public static <T> FeatureValue<T> unknown() {
        return new FeatureValue<>(null, FeatureState.UNKNOWN);
    }

    /**
     * Convenience factory for a not‑applicable value.
     */
    public static <T> FeatureValue<T> notApplicable() {
        return new FeatureValue<>(null, FeatureState.NOT_APPLICABLE);
    }
}

