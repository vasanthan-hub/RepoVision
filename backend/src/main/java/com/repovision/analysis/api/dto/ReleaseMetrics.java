package com.repovision.analysis.api.dto;

import java.time.Instant;

public record ReleaseMetrics(
        String tagName,
        Instant releaseDate,
        String commitHash,
        String message,
        long daysSincePreviousRelease,
        long commitsCount,
        long churn
) {
}
