package com.scholarmate.entity;

import java.util.Collection;
import java.util.Objects;

public record FeatureStatusValue(FeatureStatus status, Object value) {

	public FeatureStatusValue {
		Objects.requireNonNull(status, "Feature status is required");
		if ((status == FeatureStatus.KNOWN) != (value != null)) {
			throw new IllegalArgumentException("Only KNOWN features may carry a value");
		}
		if (value instanceof String text && text.isBlank()) {
			throw new IllegalArgumentException("KNOWN string features cannot be blank");
		}
		if (value instanceof Collection<?> collection && collection.isEmpty()) {
			throw new IllegalArgumentException("KNOWN list features cannot be empty");
		}
	}
}