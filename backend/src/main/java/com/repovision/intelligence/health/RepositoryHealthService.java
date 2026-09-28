package com.repovision.intelligence.health;

import com.repovision.analysis.api.dto.BusFactorResponse;
import com.repovision.analysis.api.dto.FileMetrics;
import com.repovision.analysis.application.FileAnalyticsService;
import com.repovision.analysis.application.OwnershipAnalyticsService;
import com.repovision.branch.api.dto.BranchAnalyticsResponse;
import com.repovision.branch.application.BranchAnalyticsService;
import com.repovision.commit.application.CommitStore;
import com.repovision.commit.domain.Commit;
import com.repovision.git.GitClient;
import com.repovision.git.GitCommandResult;
import com.repovision.repository.application.RepositoryStore;
import com.repovision.repository.domain.Repository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class RepositoryHealthService {

    private final CommitStore commitStore;
    private final FileAnalyticsService fileAnalyticsService;
    private final OwnershipAnalyticsService ownershipAnalyticsService;
    private final BranchAnalyticsService branchAnalyticsService;
    private final RepositoryStore repositoryStore;
    private final GitClient gitClient;

    public RepositoryHealthService(
            CommitStore commitStore,
            FileAnalyticsService fileAnalyticsService,
            OwnershipAnalyticsService ownershipAnalyticsService,
            BranchAnalyticsService branchAnalyticsService,
            RepositoryStore repositoryStore,
            GitClient gitClient
    ) {
        this.commitStore = commitStore;
        this.fileAnalyticsService = fileAnalyticsService;
        this.ownershipAnalyticsService = ownershipAnalyticsService;
        this.branchAnalyticsService = branchAnalyticsService;
        this.repositoryStore = repositoryStore;
        this.gitClient = gitClient;
    }

    public RepositoryHealthResponse calculateHealth(UUID repositoryId) {
        List<Commit> commits = commitStore.findByRepositoryId(repositoryId);
        List<FileMetrics> fileMetrics = fileAnalyticsService.computeFileMetrics(repositoryId);
        BusFactorResponse busFactor = ownershipAnalyticsService.getBusFactor(repositoryId);

        BranchAnalyticsResponse branchAnalytics;
        try {
            branchAnalytics = branchAnalyticsService.getBranchAnalytics(repositoryId);
            if (branchAnalytics == null) {
                branchAnalytics = new BranchAnalyticsResponse(1, 1, 0, List.of());
            }
        } catch (Exception e) {
            branchAnalytics = new BranchAnalyticsResponse(1, 1, 0, List.of());
        }

        // 1. Activity Score (Weight 20%)
        HealthDimension activityDim = computeActivityDimension(commits);

        // 2. Stability Score (Weight 25%)
        HealthDimension stabilityDim = computeStabilityDimension(commits, fileMetrics);

        // 3. Branch Health Score (Weight 20%)
        HealthDimension branchHealthDim = computeBranchHealthDimension(branchAnalytics);

        // 4. Ownership Score (Weight 20%)
        HealthDimension ownershipDim = computeOwnershipDimension(busFactor);

        // 5. Release Velocity Score (Weight 15%)
        HealthDimension releaseVelocityDim = computeReleaseVelocityDimension(repositoryId, commits);

        double weightedTotal = (activityDim.score() * activityDim.weight())
                + (stabilityDim.score() * stabilityDim.weight())
                + (branchHealthDim.score() * branchHealthDim.weight())
                + (ownershipDim.score() * ownershipDim.weight())
                + (releaseVelocityDim.score() * releaseVelocityDim.weight());

        int overallScore = (int) Math.round(weightedTotal);
        overallScore = Math.max(0, Math.min(100, overallScore));

        String overallStatus;
        if (overallScore >= 85) {
            overallStatus = "EXCELLENT";
        } else if (overallScore >= 70) {
            overallStatus = "GOOD";
        } else if (overallScore >= 50) {
            overallStatus = "NEEDS_ATTENTION";
        } else {
            overallStatus = "CRITICAL";
        }

        String summary = String.format(
                "Repository demonstrates %s overall engineering health (Score: %d/100). Strongest vector: %s (%.0f/100). Key focus area: %s (%.0f/100).",
                overallStatus.toLowerCase(),
                overallScore,
                getStrongestDimensionName(activityDim, stabilityDim, branchHealthDim, ownershipDim, releaseVelocityDim),
                getStrongestScore(activityDim, stabilityDim, branchHealthDim, ownershipDim, releaseVelocityDim),
                getWeakestDimensionName(activityDim, stabilityDim, branchHealthDim, ownershipDim, releaseVelocityDim),
                getWeakestScore(activityDim, stabilityDim, branchHealthDim, ownershipDim, releaseVelocityDim)
        );

        List<HealthDimension> dimensions = List.of(
                activityDim,
                stabilityDim,
                branchHealthDim,
                ownershipDim,
                releaseVelocityDim
        );

        return new RepositoryHealthResponse(
                overallScore,
                overallStatus,
                activityDim.score(),
                stabilityDim.score(),
                branchHealthDim.score(),
                ownershipDim.score(),
                releaseVelocityDim.score(),
                dimensions,
                summary
        );
    }

    private HealthDimension computeActivityDimension(List<Commit> commits) {
        if (commits.isEmpty()) {
            return new HealthDimension("Activity", 0.0, 0.20, "CRITICAL",
                    "Measures commit cadence, regularity, and active authoring days.",
                    List.of("No commit activity detected in this repository."));
        }

        Set<String> activeDays = new HashSet<>();
        for (Commit c : commits) {
            if (c.getAuthoredAt() != null) {
                activeDays.add(c.getAuthoredAt().toString().substring(0, 10));
            }
        }

        double score = 60.0;
        List<String> evidence = new ArrayList<>();
        evidence.add(String.format("Total commits: %d across %d distinct active calendar days.", commits.size(), activeDays.size()));

        if (commits.size() >= 10) score += 15.0;
        else if (commits.size() >= 5) score += 10.0;

        if (activeDays.size() >= 5) score += 15.0;
        else if (activeDays.size() >= 2) score += 10.0;

        // Check recent commits
        Instant now = Instant.now();
        long commitsLast30Days = commits.stream()
                .filter(c -> c.getAuthoredAt() != null && Duration.between(c.getAuthoredAt(), now).toDays() <= 30)
                .count();

        if (commitsLast30Days > 0) {
            score += 10.0;
            evidence.add(String.format("%d commit(s) authored in the last 30 days.", commitsLast30Days));
        } else {
            evidence.add("No commits authored within the past 30 days.");
        }

        score = Math.min(100.0, Math.max(20.0, score));
        String status = score >= 80 ? "EXCELLENT" : (score >= 65 ? "GOOD" : "NEEDS_ATTENTION");

        return new HealthDimension("Activity", Math.round(score * 10.0) / 10.0, 0.20, status,
                "Evaluates commit frequency, authoring consistency, and active engineering days.", evidence);
    }

    private HealthDimension computeStabilityDimension(List<Commit> commits, List<FileMetrics> fileMetrics) {
        double score = 90.0;
        List<String> evidence = new ArrayList<>();

        if (fileMetrics.isEmpty()) {
            return new HealthDimension("Stability", 85.0, 0.25, "GOOD",
                    "Evaluates absence of churn volatility, megacommits, and severe hotspots.",
                    List.of("Baseline stability maintained; no file change volatility detected."));
        }

        long severeHotspots = fileMetrics.stream().filter(f -> f.hotspotScore() >= 75.0).count();
        if (severeHotspots > 0) {
            double penalty = Math.min(25.0, severeHotspots * 6.0);
            score -= penalty;
            evidence.add(String.format("%d severe maintenance hotspot(s) detected with score >= 75 (Penalty: -%.0f pts).", severeHotspots, penalty));
        } else {
            evidence.add("No severe architectural hotspots detected.");
        }

        long largeCommits = commits.stream().filter(c -> (c.getAdditions() + c.getDeletions()) > 1000).count();
        if (largeCommits > 0) {
            double penalty = Math.min(20.0, largeCommits * 5.0);
            score -= penalty;
            evidence.add(String.format("%d anomalously large commit(s) exceeding 1,000 lines churn (Penalty: -%.0f pts).", largeCommits, penalty));
        } else {
            evidence.add("Commit sizes remain controlled with no extreme megacommits.");
        }

        score = Math.min(100.0, Math.max(30.0, score));
        String status = score >= 80 ? "EXCELLENT" : (score >= 65 ? "GOOD" : "NEEDS_ATTENTION");

        return new HealthDimension("Stability", Math.round(score * 10.0) / 10.0, 0.25, status,
                "Measures code churn moderation, absence of extreme commits, and file volatility control.", evidence);
    }

    private HealthDimension computeBranchHealthDimension(BranchAnalyticsResponse branchAnalytics) {
        double score = 95.0;
        List<String> evidence = new ArrayList<>();

        int totalBranches = branchAnalytics.totalBranches();
        int staleBranches = branchAnalytics.staleBranches();

        evidence.add(String.format("Total branches tracked: %d (Active: %d, Stale: %d).",
                totalBranches, branchAnalytics.activeBranches(), staleBranches));

        if (totalBranches > 0 && staleBranches > 0) {
            double staleRatio = (double) staleBranches / totalBranches;
            double penalty = staleRatio * 40.0;
            score -= penalty;
            evidence.add(String.format("%.1f%% of branches are stale (> 30 days inactive without merge) (Penalty: -%.0f pts).",
                    staleRatio * 100.0, penalty));
        } else {
            evidence.add("All branches have active commit activity or have been cleaned up.");
        }

        score = Math.min(100.0, Math.max(40.0, score));
        String status = score >= 80 ? "EXCELLENT" : (score >= 65 ? "GOOD" : "NEEDS_ATTENTION");

        return new HealthDimension("Branch Health", Math.round(score * 10.0) / 10.0, 0.20, status,
                "Evaluates branch lifecycle hygiene, merge cadence, and elimination of abandoned branches.", evidence);
    }

    private HealthDimension computeOwnershipDimension(BusFactorResponse busFactor) {
        double score = 85.0;
        List<String> evidence = new ArrayList<>();

        evidence.add(String.format("Estimated Bus Factor: %d key developer(s).", busFactor.busFactor()));

        if (busFactor.totalFiles() > 0) {
            double singleContributorRatio = (double) busFactor.singleContributorFilesCount() / busFactor.totalFiles();
            evidence.add(String.format("%.1f%% of codebase files have only 1 contributor.", singleContributorRatio * 100.0));

            if (busFactor.busFactor() == 1 && busFactor.totalFiles() > 5) {
                score -= 25.0;
                evidence.add("Critical knowledge concentration: Repository bus factor is 1 (Penalty: -25 pts).");
            } else if (busFactor.busFactor() == 2) {
                score -= 10.0;
                evidence.add("Moderate knowledge concentration: Repository bus factor is 2 (Penalty: -10 pts).");
            }

            if (singleContributorRatio > 0.80) {
                score -= 15.0;
                evidence.add("Over 80% of files have only been modified by a single author.");
            }
        }

        score = Math.min(100.0, Math.max(30.0, score));
        String status = score >= 80 ? "EXCELLENT" : (score >= 65 ? "GOOD" : "NEEDS_ATTENTION");

        return new HealthDimension("Ownership & Knowledge", Math.round(score * 10.0) / 10.0, 0.20, status,
                "Assesses collaborative distribution, domain redundancy, and avoidance of single-point-of-failure risk.", evidence);
    }

    private HealthDimension computeReleaseVelocityDimension(UUID repositoryId, List<Commit> commits) {
        double score = 70.0;
        List<String> evidence = new ArrayList<>();

        Optional<Repository> repoOpt = repositoryStore.findById(repositoryId);
        if (repoOpt.isEmpty()) {
            return new HealthDimension("Release Velocity", 70.0, 0.15, "GOOD",
                    "Tracks release cadence, tags, and deliverable iteration cycles.",
                    List.of("Repository metadata unavailable."));
        }

        GitCommandResult tagResult = gitClient.execute(
                repoOpt.get().getLocalPath(),
                List.of("tag", "-l")
        );

        if (tagResult.isSuccessful() && !tagResult.standardOutput().isBlank()) {
            String[] tags = tagResult.standardOutput().trim().split("\\R");
            evidence.add(String.format("Found %d Git release tag(s) indicating formal release milestone tracking.", tags.length));
            score += Math.min(25.0, tags.length * 8.0);
        } else {
            evidence.add("No formal Git release tags found; assessing velocity via trunk commit cadence.");
            if (commits.size() >= 10) {
                score += 10.0;
                evidence.add(String.format("Consistent trunk delivery demonstrated across %d commits.", commits.size()));
            }
        }

        score = Math.min(100.0, Math.max(30.0, score));
        String status = score >= 80 ? "EXCELLENT" : (score >= 65 ? "GOOD" : "NEEDS_ATTENTION");

        return new HealthDimension("Release Velocity", Math.round(score * 10.0) / 10.0, 0.15, status,
                "Tracks release cadence, version tagging discipline, and continuous deliverable cadence.", evidence);
    }

    private String getStrongestDimensionName(HealthDimension... dims) {
        return Arrays.stream(dims).max(Comparator.comparingDouble(HealthDimension::score)).map(HealthDimension::name).orElse("Activity");
    }

    private double getStrongestScore(HealthDimension... dims) {
        return Arrays.stream(dims).mapToDouble(HealthDimension::score).max().orElse(0.0);
    }

    private String getWeakestDimensionName(HealthDimension... dims) {
        return Arrays.stream(dims).min(Comparator.comparingDouble(HealthDimension::score)).map(HealthDimension::name).orElse("Ownership");
    }

    private double getWeakestScore(HealthDimension... dims) {
        return Arrays.stream(dims).mapToDouble(HealthDimension::score).min().orElse(0.0);
    }
}
