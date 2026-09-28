package com.repovision.intelligence.health;

import java.util.List;

public record HealthDimension(
        String name,
        double score,
        double weight,
        String status,
        String description,
        List<String> evidence
) {
}
