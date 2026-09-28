package com.repovision.intelligence.risk;

import com.repovision.analysis.api.dto.FileCoupling;
import com.repovision.analysis.api.dto.FileMetrics;
import com.repovision.analysis.api.dto.FileOwnership;
import com.repovision.analysis.application.ChangeCouplingService;
import com.repovision.analysis.application.FileAnalyticsService;
import com.repovision.analysis.application.OwnershipAnalyticsService;
import com.repovision.branch.api.dto.BranchMetrics;
import com.repovision.branch.application.BranchAnalyticsService;
import com.repovision.commit.application.CommitStore;
import com.repovision.commit.domain.Commit;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class RiskEngineService {

    private final FileAnalyticsService fileAnalyticsService;
    private final OwnershipAnalyticsService ownershipAnalyticsService;
    private final BranchAnalyticsService branchAnalyticsService;
    private final CommitStore commitStore;
    private final ChangeCouplingService changeCouplingService;

    public RiskEngineService(
            FileAnalyticsService fileAnalyticsService,
            OwnershipAnalyticsService ownershipAnalyticsService,
            BranchAnalyticsService branchAnalyticsService,
            CommitStore commitStore,
            ChangeCouplingService changeCouplingService
    ) {
        this.fileAnalyticsService = fileAnalyticsService;
        this.ownershipAnalyticsService = ownershipAnalyticsService;
        this.branchAnalyticsService = branchAnalyticsService;
        this.commitStore = commitStore;
        this.changeCouplingService = changeCouplingService;
    }

    public RiskReportResponse analyzeRisks(UUID repositoryId) {
        List<RiskItem> risks = new ArrayList<>();

        // 1. Hotspot Risks & High Churn Risks
        List<FileMetrics> fileMetrics = fileAnalyticsService.computeFileMetrics(repositoryId);
        long totalRepoChurn = fileMetrics.stream().mapToLong(FileMetrics::churn).sum();
        double avgFileChurn = fileMetrics.isEmpty() ? 0 : (double) totalRepoChurn / fileMetrics.size();

        for (FileMetrics fm : fileMetrics) {
            if (fm.hotspotScore() >= 65.0) {
                RiskSeverity severity = fm.hotspotScore() >= 80.0 ? RiskSeverity.HIGH : RiskSeverity.MEDIUM;
                risks.add(new RiskItem(
                        "hotspot-" + UUID.nameUUIDFromBytes(fm.filePath().getBytes()),
                        RiskType.HOTSPOT,
                        severity,
                        "Maintenance Hotspot Detected",
                        fm.filePath(),
                        String.format("Hotspot Score: %.1f/100, modified %d times with %d lines churn across %d contributor(s).",
                                fm.hotspotScore(), fm.modificationCount(), fm.churn(), fm.contributorCount()),
                        "This file experiences high modification velocity and churn, creating a significant probability of regression introduction and merge friction.",
                        "Review architectural boundaries, decompose responsibilities, and ensure comprehensive unit and integration test coverage for this component."
                ));
            } else if (fm.churn() > 400 && fm.churn() > avgFileChurn * 2.5) {
                risks.add(new RiskItem(
                        "churn-" + UUID.nameUUIDFromBytes(fm.filePath().getBytes()),
                        RiskType.HIGH_CHURN,
                        RiskSeverity.MEDIUM,
                        "Elevated Code Churn",
                        fm.filePath(),
                        String.format("Accumulated %d lines churn (+%d, -%d), exceeding 2.5x repository average file churn (%.0f).",
                                fm.churn(), fm.additions(), fm.deletions(), avgFileChurn),
                        "High code churn is a leading indicator of volatile requirements, incomplete specifications, or frequent defect fixes.",
                        "Stabilize interface contracts and isolate volatile implementation details behind abstractions."
                ));
            }
        }

        // 2. Ownership Concentration & Bus Factor Risks
        List<FileOwnership> ownerships = ownershipAnalyticsService.computeFileOwnerships(repositoryId);
        for (FileOwnership fo : ownerships) {
            if (fo.totalChurn() > 150 && fo.primaryContributorPercentage() >= 90.0) {
                RiskSeverity severity = fo.primaryContributorPercentage() >= 95.0 ? RiskSeverity.HIGH : RiskSeverity.MEDIUM;
                risks.add(new RiskItem(
                        "ownership-" + UUID.nameUUIDFromBytes(fo.filePath().getBytes()),
                        RiskType.OWNERSHIP_CONCENTRATION,
                        severity,
                        "Single-Author Knowledge Concentration",
                        fo.filePath(),
                        String.format("%.1f%% of all changes authored solely by %s (%s). Total churn: %d lines.",
                                fo.primaryContributorPercentage(), fo.primaryContributorName(), fo.primaryContributorEmail(), fo.totalChurn()),
                        "Knowledge regarding this file is concentrated in a single engineer, introducing bus-factor vulnerability and code review blindness.",
                        "Schedule pair programming or secondary code review assignments on future PRs touching this file to distribute domain context."
                ));
            }
        }

        // 3. Stale Branch Risks
        try {
            var branchAnalytics = branchAnalyticsService.getBranchAnalytics(repositoryId);
            for (BranchMetrics bm : branchAnalytics.branches()) {
                if (bm.isStale()) {
                    RiskSeverity severity = bm.daysInactive() > 90 ? RiskSeverity.HIGH : RiskSeverity.MEDIUM;
                    risks.add(new RiskItem(
                            "branch-" + UUID.nameUUIDFromBytes(bm.name().getBytes()),
                            RiskType.STALE_BRANCH,
                            severity,
                            "Stale / Abandoned Branch",
                            bm.name(),
                            String.format("Inactive for %d days. Last commit by %s: \"%s\".",
                                    bm.daysInactive(), bm.latestCommitAuthor(), bm.latestCommitSubject()),
                            "Unmerged branches with extended dormancy diverge significantly from trunk, causing merge debt and hidden defect regressions if re-integrated.",
                            "Determine whether to rebase and merge or delete this obsolete branch to maintain clean repository hygiene."
                    ));
                }
            }
        } catch (Exception ignored) {
            // Git branch discovery might fail if repo is empty
        }

        // 4. Large Commit Risks
        List<Commit> commits = commitStore.findByRepositoryId(repositoryId);
        if (!commits.isEmpty()) {
            double avgCommitSize = commits.stream().mapToLong(c -> c.getAdditions() + c.getDeletions()).average().orElse(0.0);
            for (Commit c : commits) {
                long commitChurn = c.getAdditions() + c.getDeletions();
                if (commitChurn > 1000 || (commitChurn > 500 && commitChurn > avgCommitSize * 4.0)) {
                    RiskSeverity severity = commitChurn > 1500 ? RiskSeverity.HIGH : RiskSeverity.MEDIUM;
                    risks.add(new RiskItem(
                            "commit-" + c.getHash().substring(0, Math.min(8, c.getHash().length())),
                            RiskType.LARGE_COMMIT,
                            severity,
                            "Anomalously Large Commit",
                            c.getHash().substring(0, Math.min(8, c.getHash().length())),
                            String.format("Commit had %d lines churn (+%d, -%d). Subject: \"%s\" by %s.",
                                    commitChurn, c.getAdditions(), c.getDeletions(), c.getSubject(), c.getAuthorName()),
                            "Megacommits are difficult to review effectively, often bypass thorough quality checks, and complicate git bisect root-cause investigation.",
                            "Enforce branch-based atomic commit guidelines and break multi-responsibility feature sets into small pull requests."
                    ));
                }
            }
        }

        // 5. Strong Change Coupling Risks
        List<FileCoupling> couplings = changeCouplingService.computeCoupledFiles(repositoryId);
        for (FileCoupling fc : couplings) {
            if ("STRONG".equals(fc.strength()) && fc.coChangeCount() >= 3) {
                risks.add(new RiskItem(
                        "coupling-" + UUID.nameUUIDFromBytes((fc.fileA() + ":" + fc.fileB()).getBytes()),
                        RiskType.STRONG_CHANGE_COUPLING,
                        RiskSeverity.MEDIUM,
                        "High Implicit Architectural Coupling",
                        fc.fileA() + " <-> " + fc.fileB(),
                        String.format("Files changed together in %d commits (%.1f%% co-change frequency).",
                                fc.coChangeCount(), fc.couplingPercentage()),
                        "Files that consistently change in tandem across separate modules indicate tight coupling, shared hidden assumptions, or leaky abstractions.",
                        "Assess if shared logic should be encapsulated into a common interface, value object, or dedicated service component."
                ));
            }
        }

        // 6. Declining Activity Risk
        if (commits.size() >= 5) {
            Instant now = Instant.now();
            long recent30DaysCount = commits.stream()
                    .filter(c -> c.getAuthoredAt() != null && Duration.between(c.getAuthoredAt(), now).toDays() <= 30)
                    .count();
            long prior30DaysCount = commits.stream()
                    .filter(c -> c.getAuthoredAt() != null && Duration.between(c.getAuthoredAt(), now).toDays() > 30 && Duration.between(c.getAuthoredAt(), now).toDays() <= 60)
                    .count();

            if (prior30DaysCount >= 4 && recent30DaysCount == 0) {
                risks.add(new RiskItem(
                        "declining-activity",
                        RiskType.DECLINING_ACTIVITY,
                        RiskSeverity.LOW,
                        "Repository Activity Decline",
                        "Repository Velocity",
                        String.format("0 commits recorded in the last 30 days compared to %d commits in the preceding 30-day interval.", prior30DaysCount),
                        "Activity has stalled or dropped significantly, which may indicate dormant maintenance or stalled engineering sprints.",
                        "Verify sprint alignment and review backlog prioritisation if the repository is expected to be under active development."
                ));
            }
        }

        // Sort risks: HIGH first, then MEDIUM, then LOW
        risks.sort((a, b) -> {
            int cmp = b.severity().compareTo(a.severity());
            if (cmp != 0) return cmp;
            return a.type().compareTo(b.type());
        });

        int highCount = (int) risks.stream().filter(r -> r.severity() == RiskSeverity.HIGH).count();
        int mediumCount = (int) risks.stream().filter(r -> r.severity() == RiskSeverity.MEDIUM).count();
        int lowCount = (int) risks.stream().filter(r -> r.severity() == RiskSeverity.LOW).count();

        return new RiskReportResponse(risks.size(), highCount, mediumCount, lowCount, risks);
    }
}
