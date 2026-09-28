package com.repovision.analysis.application;

import com.repovision.analysis.api.dto.*;
import com.repovision.branch.api.dto.BranchAnalyticsResponse;
import com.repovision.branch.application.BranchAnalyticsService;
import com.repovision.commit.application.CommitStore;
import com.repovision.commit.domain.Commit;
import com.repovision.intelligence.health.RepositoryHealthResponse;
import com.repovision.intelligence.health.RepositoryHealthService;
import com.repovision.intelligence.insight.InsightEngineService;
import com.repovision.intelligence.insight.InsightReportResponse;
import com.repovision.intelligence.risk.RiskEngineService;
import com.repovision.intelligence.risk.RiskReportResponse;
import com.repovision.repository.application.RepositoryService;
import com.repovision.repository.api.dto.RepositoryResponse;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class OverviewService {

    private final RepositoryService repositoryService;
    private final CommitStore commitStore;
    private final FileAnalyticsService fileAnalyticsService;
    private final BranchAnalyticsService branchAnalyticsService;
    private final RepositoryHealthService repositoryHealthService;
    private final RiskEngineService riskEngineService;
    private final InsightEngineService insightEngineService;

    public OverviewService(
            RepositoryService repositoryService,
            CommitStore commitStore,
            FileAnalyticsService fileAnalyticsService,
            BranchAnalyticsService branchAnalyticsService,
            RepositoryHealthService repositoryHealthService,
            RiskEngineService riskEngineService,
            InsightEngineService insightEngineService
    ) {
        this.repositoryService = repositoryService;
        this.commitStore = commitStore;
        this.fileAnalyticsService = fileAnalyticsService;
        this.branchAnalyticsService = branchAnalyticsService;
        this.repositoryHealthService = repositoryHealthService;
        this.riskEngineService = riskEngineService;
        this.insightEngineService = insightEngineService;
    }

    public RepositoryOverviewResponse getOverview(UUID repositoryId) {
        RepositoryResponse repository = repositoryService.getRepository(repositoryId);
        List<Commit> commits = commitStore.findByRepositoryId(repositoryId);

        long totalCommits = commits.size();
        long totalAdditions = commits.stream().mapToLong(Commit::getAdditions).sum();
        long totalDeletions = commits.stream().mapToLong(Commit::getDeletions).sum();
        long totalChurn = totalAdditions + totalDeletions;

        int totalDevelopers = (int) commits.stream()
                .map(Commit::getAuthorEmail)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        List<FileMetrics> fileMetrics = fileAnalyticsService.computeFileMetrics(repositoryId);
        int totalFiles = fileMetrics.size();
        List<FileMetrics> topHotspots = fileMetrics.stream()
                .sorted(Comparator.comparingDouble(FileMetrics::hotspotScore).reversed())
                .limit(5)
                .toList();

        BranchAnalyticsResponse branchAnalytics;
        try {
            branchAnalytics = branchAnalyticsService.getBranchAnalytics(repositoryId);
        } catch (Exception e) {
            branchAnalytics = new BranchAnalyticsResponse(1, 1, 0, List.of());
        }

        // Recent activity
        Instant now = Instant.now();
        long commits7 = 0;
        long commits30 = 0;
        long commits90 = 0;
        Set<String> activeDays = new HashSet<>();

        for (Commit c : commits) {
            if (c.getAuthoredAt() != null) {
                activeDays.add(c.getAuthoredAt().toString().substring(0, 10));
                long days = Duration.between(c.getAuthoredAt(), now).toDays();
                if (days <= 7) commits7++;
                if (days <= 30) commits30++;
                if (days <= 90) commits90++;
            }
        }
        RecentActivitySummary recentActivity = new RecentActivitySummary(commits7, commits30, commits90, activeDays.size());

        RepositoryHealthResponse health = repositoryHealthService.calculateHealth(repositoryId);
        RiskReportResponse risks = riskEngineService.analyzeRisks(repositoryId);
        InsightReportResponse insightReport = insightEngineService.generateInsights(repositoryId);

        return new RepositoryOverviewResponse(
                repository,
                health,
                totalCommits,
                totalDevelopers,
                totalFiles,
                branchAnalytics.totalBranches(),
                branchAnalytics.activeBranches(),
                branchAnalytics.staleBranches(),
                totalChurn,
                recentActivity,
                topHotspots,
                risks,
                insightReport.insights()
        );
    }
}
