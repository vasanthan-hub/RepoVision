package com.repovision.intelligence.insight;

public record InsightItem(
        String id,
        InsightCategory category,
        InsightType type,
        String title,
        String finding,
        String interpretation,
        String action
) {
}
