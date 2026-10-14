package com.scholarmate.dto;

import java.time.LocalDateTime;

public record ScholarshipCatalogStatus(
        long scholarshipCount,
        long activeSourceCount,
        LocalDateTime lastSuccessfulRefresh,
        boolean stale) {
}
