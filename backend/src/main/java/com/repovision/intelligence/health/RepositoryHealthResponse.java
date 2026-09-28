package com.repovision.intelligence.health;

import java.util.List;

public record RepositoryHealthResponse(
        int overallScore,
        String status,
        double activityScore,
        double stabilityScore,
        double branchHealthScore,
        double ownershipScore,
        double releaseVelocityScore,
        List<HealthDimension> dimensions,
        String summary
) {
}
