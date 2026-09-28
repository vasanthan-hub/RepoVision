package com.repovision.intelligence.insight;

import java.util.List;

public record InsightReportResponse(
        int totalInsights,
        List<InsightItem> insights
) {
}
