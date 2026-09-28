package com.repovision.intelligence.insight;

import com.repovision.analysis.api.dto.BusFactorResponse;
import com.repovision.analysis.api.dto.FileMetrics;
import com.repovision.analysis.api.dto.FileOwnership;
import com.repovision.analysis.application.FileAnalyticsService;
import com.repovision.analysis.application.OwnershipAnalyticsService;
import com.repovision.branch.api.dto.BranchAnalyticsResponse;
import com.repovision.branch.api.dto.BranchMetrics;
import com.repovision.branch.application.BranchAnalyticsService;
import com.repovision.commit.application.CommitStore;
import com.repovision.commit.domain.Commit;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class InsightEngineService {

    private final FileAnalyticsService fileAnalyticsService;
    private final OwnershipAnalyticsService ownershipAnalyticsService;
    private final BranchAnalyticsService branchAnalyticsService;
    private final CommitStore commitStore;

    public InsightEngineService(
            FileAnalyticsService fileAnalyticsService,
            OwnershipAnalyticsService ownershipAnalyticsService,
            BranchAnalyticsService branchAnalyticsService,
            CommitStore commitStore
    ) {
        this.fileAnalyticsService = fileAnalyticsService;
        this.ownershipAnalyticsService = ownershipAnalyticsService;
        this.branchAnalyticsService = branchAnalyticsService;
        this.commitStore = commitStore;
    }

    public InsightReportResponse generateInsights(UUID repositoryId) {
        List<InsightItem> insights = new ArrayList<>();

        List<FileMetrics> fileMetrics = fileAnalyticsService.computeFileMetrics(repositoryId);
        BusFactorResponse busFactor = ownershipAnalyticsService.getBusFactor(repositoryId);
        List<Commit> commits = commitStore.findByRepositoryId(repositoryId);

        BranchAnalyticsResponse branchAnalytics;
        try {
            branchAnalytics = branchAnalyticsService.getBranchAnalytics(repositoryId);
        } catch (Exception e) {
            branchAnalytics = new BranchAnalyticsResponse(0, 0, 0, List.of());
        }

        // 1. Architecture: Hotspot Insight
        if (!fileMetrics.isEmpty()) {
            FileMetrics topHotspot = fileMetrics.stream()
                    .max(Comparator.comparingDouble(FileMetrics::hotspotScore))
                    .orElse(fileMetrics.get(0));

            if (topHotspot.hotspotScore() >= 60.0) {
                insights.add(new InsightItem(
                        "insight-hotspot-top",
                        InsightCategory.ARCHITECTURE,
                        topHotspot.hotspotScore() >= 75.0 ? InsightType.CRITICAL : InsightType.WARNING,
                        "Primary Architectural Hotspot Identified",
                        String.format("File '%s' changed %d times with %d lines churn (Hotspot Score: %.1f/100).",
                                topHotspot.filePath(), topHotspot.modificationCount(), topHotspot.churn(), topHotspot.hotspotScore()),
                        "This file acts as a gravitational center for repository modifications, experiencing repeated edits across features and bug fixes.",
                        "Evaluate if the component has accumulated multiple disparate responsibilities. Refactor into decoupled services and expand automated unit tests."
                ));
            } else {
                insights.add(new InsightItem(
                        "insight-hotspot-clean",
                        InsightCategory.ARCHITECTURE,
                        InsightType.POSITIVE,
                        "Controlled Component Modification Volatility",
                        String.format("Top file change frequency is %d commits, with no severe hotspots exceeding 60.0 score.", topHotspot.modificationCount()),
                        "Changes are evenly distributed throughout modules rather than creating severe single-point maintenance bottlenecks.",
                        "Continue enforcing modular boundaries and component cohesion during code reviews."
                ));
            }
        }

        // 2. Collaboration: Bus Factor / Knowledge Concentration
        if (busFactor.totalFiles() > 0) {
            if (busFactor.busFactor() <= 1 && busFactor.totalFiles() > 5) {
                insights.add(new InsightItem(
                        "insight-bus-factor-warning",
                        InsightCategory.COLLABORATION,
                        InsightType.CRITICAL,
                        "High Knowledge Concentration Risk (Bus Factor = 1)",
                        String.format("Bus factor is 1; %d of %d files (%.1f%%) have only a single contributor.",
                                busFactor.singleContributorFilesCount(), busFactor.totalFiles(),
                                (double) busFactor.singleContributorFilesCount() / busFactor.totalFiles() * 100.0),
                        "A critical fraction of the domain knowledge resides with an individual contributor, creating severe knowledge loss risk.",
                        "Institute cross-functional pair programming and secondary code review assignments on core modules to share architectural understanding."
                ));
            } else {
                insights.add(new InsightItem(
                        "insight-collab-healthy",
                        InsightCategory.COLLABORATION,
                        InsightType.POSITIVE,
                        "Resilient Collaborative Distribution",
                        String.format("Estimated bus factor is %d with multiple active contributors sharing codebase maintenance.", busFactor.busFactor()),
                        "Knowledge is dispersed across engineering team members, reducing dependency on individual domain specialists.",
                        "Maintain open review practices and rotate module ownership regularly."
                ));
            }
        }

        // 3. Hygiene: Branch Management
        if (branchAnalytics.staleBranches() > 0) {
            BranchMetrics worstStale = branchAnalytics.branches().stream()
                    .filter(BranchMetrics::isStale)
                    .max(Comparator.comparingLong(BranchMetrics::daysInactive))
                    .orElse(null);

            if (worstStale != null) {
                insights.add(new InsightItem(
                        "insight-branch-stale",
                        InsightCategory.HYGIENE,
                        InsightType.WARNING,
                        "Dormant Feature Branches Require Cleanup",
                        String.format("%d of %d branches are stale. Oldest branch '%s' has been inactive for %d days.",
                                branchAnalytics.staleBranches(), branchAnalytics.totalBranches(), worstStale.name(), worstStale.daysInactive()),
                        "Abandoned branches accumulate divergence from trunk, clutter git refs, and increase risk of stale merges.",
                        "Execute a branch pruning review: merge completed work or delete obsolete remote branches."
                ));
            }
        } else if (branchAnalytics.totalBranches() > 0) {
            insights.add(new InsightItem(
                    "insight-branch-hygiene",
                    InsightCategory.HYGIENE,
                    InsightType.POSITIVE,
                    "Clean Branching Hygiene Maintained",
                    String.format("All %d tracked branches have active commits within the last 30 days or are cleanly integrated.", branchAnalytics.totalBranches()),
                    "The team promptly merges and retires feature branches, minimizing trunk divergence and merge debt.",
                    "Preserve current short-lived feature branch lifecycle conventions."
            ));
        }

        // 4. Velocity: Commit Granularity
        if (!commits.isEmpty()) {
            double avgSize = commits.stream().mapToLong(c -> c.getAdditions() + c.getDeletions()).average().orElse(0.0);
            long megacommitCount = commits.stream().filter(c -> (c.getAdditions() + c.getDeletions()) > 1000).count();

            if (megacommitCount > 0) {
                insights.add(new InsightItem(
                        "insight-velocity-megacommits",
                        InsightCategory.VELOCITY,
                        InsightType.WARNING,
                        "Large Batch Commits Impair Review Quality",
                        String.format("%d commit(s) exceeded 1,000 lines churn (Average commit size: %.0f lines).", megacommitCount, avgSize),
                        "Large batch changes overwhelm peer reviewers, increasing the likelihood of unspotted bugs and complicating revert operations.",
                        "Encourage atomic commits where each commit represents a single logical unit of change with corresponding tests."
                ));
            } else {
                insights.add(new InsightItem(
                        "insight-velocity-atomic",
                        InsightCategory.VELOCITY,
                        InsightType.POSITIVE,
                        "Consistent Atomic Commit Granularity",
                        String.format("Average commit size is %.0f lines churn with zero megacommits exceeding 1,000 lines.", avgSize),
                        "Commits are structured into focused, reviewable increments that support clear Git history and seamless cherry-picking.",
                        "Maintain atomic commit discipline across future pull requests."
                ));
            }
        }

        // Sort: CRITICAL -> WARNING -> INFO -> POSITIVE
        insights.sort(Comparator.comparing(InsightItem::type));

        return new InsightReportResponse(insights.size(), insights);
    }
}
