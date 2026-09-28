package com.repovision.analysis.application;

import com.repovision.analysis.api.dto.MetricDelta;
import com.repovision.analysis.api.dto.PeriodComparisonResponse;
import com.repovision.analysis.api.dto.WhatChangedResponse;
import com.repovision.commit.application.CommitStore;
import com.repovision.commit.application.FileChangeStore;
import com.repovision.commit.domain.Commit;
import com.repovision.commit.domain.FileChange;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ComparisonService {

    private final CommitStore commitStore;
    private final FileChangeStore fileChangeStore;

    public ComparisonService(
            CommitStore commitStore,
            FileChangeStore fileChangeStore
    ) {
        this.commitStore = commitStore;
        this.fileChangeStore = fileChangeStore;
    }

    public PeriodComparisonResponse comparePeriods(UUID repositoryId, int days) {
        int windowDays = days > 0 ? days : 30;

        List<Commit> allCommits = commitStore.findByRepositoryId(repositoryId);
        if (allCommits.isEmpty()) {
            Instant now = Instant.now();
            return new PeriodComparisonResponse(
                    windowDays,
                    now.minus(Duration.ofDays(windowDays)),
                    now,
                    now.minus(Duration.ofDays(windowDays * 2L)),
                    now.minus(Duration.ofDays(windowDays)),
                    Collections.emptyMap()
            );
        }

        // Determine anchor date: if latest commit is older than windowDays from now, anchor to latest commit
        Instant latestCommitDate = allCommits.stream()
                .map(Commit::getAuthoredAt)
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(Instant.now());

        Instant now = Instant.now();
        Instant currentEnd = (Duration.between(latestCommitDate, now).toDays() > windowDays)
                ? latestCommitDate.plus(Duration.ofDays(1))
                : now;

        Instant currentStart = currentEnd.minus(Duration.ofDays(windowDays));
        Instant previousEnd = currentStart;
        Instant previousStart = previousEnd.minus(Duration.ofDays(windowDays));

        // Filter commits for each period
        List<Commit> currentCommits = allCommits.stream()
                .filter(c -> c.getAuthoredAt() != null
                        && !c.getAuthoredAt().isBefore(currentStart)
                        && c.getAuthoredAt().isBefore(currentEnd))
                .toList();

        List<Commit> previousCommits = allCommits.stream()
                .filter(c -> c.getAuthoredAt() != null
                        && !c.getAuthoredAt().isBefore(previousStart)
                        && c.getAuthoredAt().isBefore(previousEnd))
                .toList();

        // Commits metrics
        MetricDelta commitsDelta = MetricDelta.of("Commits", currentCommits.size(), previousCommits.size(), "commits");

        // Churn, additions, deletions
        long curAdditions = currentCommits.stream().mapToLong(Commit::getAdditions).sum();
        long prevAdditions = previousCommits.stream().mapToLong(Commit::getAdditions).sum();
        MetricDelta additionsDelta = MetricDelta.of("Additions", curAdditions, prevAdditions, "lines");

        long curDeletions = currentCommits.stream().mapToLong(Commit::getDeletions).sum();
        long prevDeletions = previousCommits.stream().mapToLong(Commit::getDeletions).sum();
        MetricDelta deletionsDelta = MetricDelta.of("Deletions", curDeletions, prevDeletions, "lines");

        long curChurn = curAdditions + curDeletions;
        long prevChurn = prevAdditions + prevDeletions;
        MetricDelta churnDelta = MetricDelta.of("Code Churn", curChurn, prevChurn, "lines");

        // Contributors
        long curAuthors = currentCommits.stream().map(Commit::getAuthorEmail).distinct().count();
        long prevAuthors = previousCommits.stream().map(Commit::getAuthorEmail).distinct().count();
        MetricDelta contributorsDelta = MetricDelta.of("Active Contributors", curAuthors, prevAuthors, "developers");

        // Files changed
        Set<UUID> curCommitIds = currentCommits.stream().map(Commit::getId).collect(Collectors.toSet());
        Set<UUID> prevCommitIds = previousCommits.stream().map(Commit::getId).collect(Collectors.toSet());

        List<FileChange> allChanges = fileChangeStore.findByRepositoryId(repositoryId);
        long curFilesCount = allChanges.stream().filter(fc -> curCommitIds.contains(fc.getCommitId())).map(FileChange::getFilePath).distinct().count();
        long prevFilesCount = allChanges.stream().filter(fc -> prevCommitIds.contains(fc.getCommitId())).map(FileChange::getFilePath).distinct().count();
        MetricDelta filesDelta = MetricDelta.of("Files Modified", curFilesCount, prevFilesCount, "files");

        Map<String, MetricDelta> metrics = new LinkedHashMap<>();
        metrics.put("commits", commitsDelta);
        metrics.put("churn", churnDelta);
        metrics.put("additions", additionsDelta);
        metrics.put("deletions", deletionsDelta);
        metrics.put("contributors", contributorsDelta);
        metrics.put("filesModified", filesDelta);

        return new PeriodComparisonResponse(
                windowDays,
                currentStart,
                currentEnd,
                previousStart,
                previousEnd,
                metrics
        );
    }

    public WhatChangedResponse getWhatChanged(UUID repositoryId, int days) {
        PeriodComparisonResponse comparison = comparePeriods(repositoryId, days);
        Map<String, MetricDelta> metrics = comparison.metrics();

        List<String> highlights = new ArrayList<>();
        List<String> detailedChanges = new ArrayList<>();

        if (metrics.isEmpty()) {
            highlights.add("No commit activity recorded in the specified timeframe.");
            return new WhatChangedResponse(days, days + "-Day Window", highlights, detailedChanges);
        }

        MetricDelta commits = metrics.get("commits");
        MetricDelta churn = metrics.get("churn");
        MetricDelta contributors = metrics.get("contributors");
        MetricDelta files = metrics.get("filesModified");

        if (commits != null) {
            String commitMsg = String.format("Commit velocity moved by %+.1f%% (%d commits vs %d in preceding interval).",
                    commits.percentageChange(), (long) commits.currentValue(), (long) commits.previousValue());
            highlights.add(commitMsg);
            detailedChanges.add(commitMsg);
        }

        if (churn != null) {
            String churnMsg = String.format("Code churn shifted by %+.1f%% (%d lines modified vs %d lines previously).",
                    churn.percentageChange(), (long) churn.currentValue(), (long) churn.previousValue());
            highlights.add(churnMsg);
            detailedChanges.add(churnMsg);
        }

        if (contributors != null) {
            String contribMsg = String.format("Active engineering contributors shifted from %d to %d.",
                    (long) contributors.previousValue(), (long) contributors.currentValue());
            detailedChanges.add(contribMsg);
        }

        if (files != null) {
            String fileMsg = String.format("Total touched files shifted by %+.1f%% (%d files vs %d in preceding interval).",
                    files.percentageChange(), (long) files.currentValue(), (long) files.previousValue());
            detailedChanges.add(fileMsg);
        }

        String label = String.format("Last %d Days vs Preceding %d Days", comparison.days(), comparison.days());
        return new WhatChangedResponse(comparison.days(), label, highlights, detailedChanges);
    }
}
