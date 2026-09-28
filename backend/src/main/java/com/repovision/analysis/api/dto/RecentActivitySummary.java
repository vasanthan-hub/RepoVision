package com.repovision.analysis.api.dto;

public record RecentActivitySummary(
        long commitsLast7Days,
        long commitsLast30Days,
        long commitsLast90Days,
        int activeDaysCount
) {
}
