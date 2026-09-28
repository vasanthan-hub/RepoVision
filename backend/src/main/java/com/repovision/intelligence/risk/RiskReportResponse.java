package com.repovision.intelligence.risk;

import java.util.List;

public record RiskReportResponse(
        int totalRisks,
        int highSeverityCount,
        int mediumSeverityCount,
        int lowSeverityCount,
        List<RiskItem> risks
) {
}
