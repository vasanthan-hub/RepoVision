package com.repovision.analysis.api.dto;

import java.util.List;

public record ReleaseAnalyticsResponse(
        int totalReleases,
        double averageReleaseIntervalDays,
        List<ReleaseMetrics> releases
) {
}
