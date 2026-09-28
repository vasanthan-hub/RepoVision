package com.repovision.analysis.api.dto;

import com.repovision.intelligence.health.RepositoryHealthResponse;
import com.repovision.intelligence.insight.InsightItem;
import com.repovision.intelligence.risk.RiskReportResponse;
import com.repovision.repository.api.dto.RepositoryResponse;

import java.util.List;

public record RepositoryOverviewResponse(
        RepositoryResponse repository,
        RepositoryHealthResponse health,
        long totalCommits,
        int totalDevelopers,
        int totalFiles,
        int totalBranches,
        int activeBranches,
        int staleBranches,
        long totalChurn,
        RecentActivitySummary recentActivity,
        List<FileMetrics> topHotspots,
        RiskReportResponse risks,
        List<InsightItem> insights
) {
}
