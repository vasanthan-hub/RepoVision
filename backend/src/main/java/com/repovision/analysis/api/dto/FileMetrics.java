package com.repovision.analysis.api.dto;

import java.time.Instant;

public record FileMetrics(
        String filePath,
        long modificationCount,
        long additions,
        long deletions,
        long churn,
        int contributorCount,
        Instant firstModified,
        Instant lastModified,
        double averageChangeSize,
        double hotspotScore
) {
}
