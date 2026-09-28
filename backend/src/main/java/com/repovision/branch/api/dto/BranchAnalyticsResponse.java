package com.repovision.branch.api.dto;

import java.util.List;

public record BranchAnalyticsResponse(
        int totalBranches,
        int activeBranches,
        int staleBranches,
        List<BranchMetrics> branches
) {
}
