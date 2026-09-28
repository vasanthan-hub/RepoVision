package com.repovision.analysis.application;

import com.repovision.analysis.api.dto.CommitAnalyticsResponse;
import com.repovision.analysis.api.dto.CommitSummary;
import com.repovision.analysis.api.dto.CommitsByDate;
import com.repovision.commit.application.CommitStore;
import com.repovision.commit.domain.Commit;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommitAnalyticsService {

    private final CommitStore commitStore;

    public CommitAnalyticsService(
            CommitStore commitStore
    ) {
        this.commitStore = commitStore;
    }

    public CommitAnalyticsResponse getCommitAnalytics(
            UUID repositoryId
    ) {

        List<Commit> commits =
                commitStore.findByRepositoryId(repositoryId);

        long totalAdditions =
                commits.stream()
                        .mapToLong(Commit::getAdditions)
                        .sum();

        long totalDeletions =
                commits.stream()
                        .mapToLong(Commit::getDeletions)
                        .sum();

        long totalChurn =
                totalAdditions + totalDeletions;

        double averageCommitSize =
                commits.isEmpty()
                        ? 0.0
                        : (double) totalChurn / commits.size();

        double averageAdditionsPerCommit =
                commits.isEmpty()
                        ? 0.0
                        : (double) totalAdditions / commits.size();

        double averageDeletionsPerCommit =
                commits.isEmpty()
                        ? 0.0
                        : (double) totalDeletions / commits.size();

        CommitSummary largestCommit =
                commits.stream()
                        .max(
                                java.util.Comparator.comparingLong(
                                        commit ->
                                                commit.getAdditions()
                                                        + commit.getDeletions()
                                )
                        )
                        .map(commit ->
                                new CommitSummary(
                                        commit.getHash(),
                                        commit.getSubject(),
                                        commit.getAdditions(),
                                        commit.getDeletions(),
                                        commit.getAdditions()
                                                + commit.getDeletions()
                                )
                        )
                        .orElse(null);

        Map<LocalDate, Long> commitsGroupedByDate =
                commits.stream()
                        .collect(
                                Collectors.groupingBy(
                                        commit ->
                                                commit.getAuthoredAt()
                                                        .atZone(
                                                                ZoneOffset.UTC
                                                        )
                                                        .toLocalDate(),
                                        Collectors.counting()
                                )
                        );

        List<CommitsByDate> commitsByDate =
                commitsGroupedByDate.entrySet()
                        .stream()
                        .sorted(
                                Map.Entry.comparingByKey()
                        )
                        .map(entry ->
                                new CommitsByDate(
                                        entry.getKey(),
                                        entry.getValue()
                                )
                        )
                        .toList();

        double averageCommitsPerActiveDay =
                commitsByDate.isEmpty()
                        ? 0.0
                        : (double) commits.size()
                        / commitsByDate.size();

        return new CommitAnalyticsResponse(
                commits.size(),
                totalAdditions,
                totalDeletions,
                totalChurn,
                averageCommitSize,
                averageAdditionsPerCommit,
                averageDeletionsPerCommit,
                largestCommit,
                averageCommitsPerActiveDay,
                commitsByDate
        );
    }
}