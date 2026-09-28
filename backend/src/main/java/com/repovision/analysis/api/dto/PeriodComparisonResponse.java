package com.repovision.analysis.api.dto;

import java.time.Instant;
import java.util.Map;

public record PeriodComparisonResponse(
        int days,
        Instant currentPeriodStart,
        Instant currentPeriodEnd,
        Instant previousPeriodStart,
        Instant previousPeriodEnd,
        Map<String, MetricDelta> metrics
) {
}
