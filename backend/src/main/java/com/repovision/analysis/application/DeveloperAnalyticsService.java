package com.repovision.analysis.application;

import com.repovision.analysis.api.dto.DeveloperActivity;
import com.repovision.analysis.api.dto.DeveloperAnalyticsResponse;
import com.repovision.analysis.api.dto.DeveloperMetrics;
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
public class DeveloperAnalyticsService {

    private final CommitStore commitStore;

    public DeveloperAnalyticsService(
            CommitStore commitStore
    ) {
        this.commitStore = commitStore;
    }

    public DeveloperAnalyticsResponse getDeveloperAnalytics(
            UUID repositoryId
    ) {

        List<Commit> commits =
                commitStore.findByRepositoryId(repositoryId);

        long totalCommits =
                commits.size();

        long totalChurn =
                commits.stream()
                        .mapToLong(commit ->
                                commit.getAdditions()
                                        + commit.getDeletions()
                        )
                        .sum();

        Map<String, List<Commit>> commitsByDeveloper =
                commits.stream()
                        .collect(
                                Collectors.groupingBy(
                                        Commit::getAuthorEmail
                                )
                        );

        List<DeveloperMetrics> developers =
                commitsByDeveloper.entrySet()
                        .stream()
                        .map(developerEntry -> {

                            String authorEmail =
                                    developerEntry.getKey();

                            List<Commit> developerCommits =
                                    developerEntry.getValue();

                            String authorName =
                                    developerCommits.get(0)
                                            .getAuthorName();

                            long commitCount =
                                    developerCommits.size();

                            long additions =
                                    developerCommits.stream()
                                            .mapToLong(
                                                    Commit::getAdditions
                                            )
                                            .sum();

                            long deletions =
                                    developerCommits.stream()
                                            .mapToLong(
                                                    Commit::getDeletions
                                            )
                                            .sum();

                            long churn =
                                    additions + deletions;

                            double commitPercentage =
                                    totalCommits == 0
                                            ? 0.0
                                            : (double) commitCount
                                            / totalCommits
                                            * 100;

                            double churnPercentage =
                                    totalChurn == 0
                                            ? 0.0
                                            : (double) churn
                                            / totalChurn
                                            * 100;

                            Map<LocalDate, Long> activityGroupedByDate =
                                    developerCommits.stream()
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

                            List<DeveloperActivity> activityByDate =
                                    activityGroupedByDate.entrySet()
                                            .stream()
                                            .sorted(
                                                    Map.Entry.comparingByKey()
                                            )
                                            .map(activityEntry ->
                                                    new DeveloperActivity(
                                                            activityEntry.getKey(),
                                                            activityEntry.getValue()
                                                    )
                                            )
                                            .toList();

                            return new DeveloperMetrics(
                                    authorName,
                                    authorEmail,
                                    commitCount,
                                    additions,
                                    deletions,
                                    churn,
                                    commitPercentage,
                                    churnPercentage,
                                    activityByDate
                            );
                        })
                        .toList();

        return new DeveloperAnalyticsResponse(
                developers.size(),
                developers
        );
    }
}