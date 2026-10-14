package com.scholarmate.feature;

import lombok.*;

/**
 * Placeholder for behavioural signals associated with a user (e.g., recent activity,
 * clicks, time‑on‑page, etc.). The actual schema can be expanded later. For now we
 * include a minimal set needed for compilation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBehaviourFeatures {
    private Long userId;
    private Integer recentLoginCount;
    private Double averageSessionDuration; // minutes
    // More behavioural attributes can be added as the project evolves.
}

