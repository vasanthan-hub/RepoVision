package com.repovision.intelligence.risk;

public record RiskItem(
        String id,
        RiskType type,
        RiskSeverity severity,
        String title,
        String entity,
        String evidence,
        String description,
        String recommendation
) {
}
