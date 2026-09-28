package com.repovision.analysis.application;

import com.repovision.analysis.api.dto.ReleaseAnalyticsResponse;
import com.repovision.analysis.api.dto.ReleaseMetrics;
import com.repovision.git.GitClient;
import com.repovision.git.GitCommandResult;
import com.repovision.repository.application.RepositoryStore;
import com.repovision.repository.domain.Repository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class ReleaseAnalyticsService {

    private final RepositoryStore repositoryStore;
    private final GitClient gitClient;

    public ReleaseAnalyticsService(
            RepositoryStore repositoryStore,
            GitClient gitClient
    ) {
        this.repositoryStore = repositoryStore;
        this.gitClient = gitClient;
    }

    public ReleaseAnalyticsResponse getReleaseAnalytics(UUID repositoryId) {
        Repository repository = repositoryStore.findById(repositoryId)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found with id: " + repositoryId));

        GitCommandResult result = gitClient.execute(
                repository.getLocalPath(),
                List.of(
                        "tag",
                        "-l",
                        "--sort=creatordate",
                        "--format=%(refname:short)%09%(creatordate:iso-strict)%09%(objectname)%09%(subject)"
                )
        );

        if (!result.isSuccessful() || result.standardOutput().isBlank()) {
            return new ReleaseAnalyticsResponse(0, 0.0, List.of());
        }

        record TagRecord(String name, Instant date, String hash, String subject) {}
        List<TagRecord> tags = new ArrayList<>();

        for (String line : result.standardOutput().split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            String[] parts = trimmed.split("\t", -1);
            if (parts.length < 3) continue;

            String name = parts[0].trim();
            String dateStr = parts[1].trim();
            String hash = parts[2].trim();
            String subject = parts.length > 3 ? parts[3].trim() : "";

            Instant date;
            try {
                date = OffsetDateTime.parse(dateStr).toInstant();
            } catch (DateTimeParseException e) {
                date = Instant.now();
            }

            tags.add(new TagRecord(name, date, hash, subject));
        }

        if (tags.isEmpty()) {
            return new ReleaseAnalyticsResponse(0, 0.0, List.of());
        }

        List<ReleaseMetrics> releases = new ArrayList<>();
        long totalIntervalDays = 0;
        int intervalCount = 0;

        for (int i = 0; i < tags.size(); i++) {
            TagRecord current = tags.get(i);
            long daysSincePrevious = 0;
            long commitsCount = 0;
            long churn = 0;

            if (i > 0) {
                TagRecord prev = tags.get(i - 1);
                daysSincePrevious = Math.max(0, Duration.between(prev.date(), current.date()).toDays());
                totalIntervalDays += daysSincePrevious;
                intervalCount++;

                // Git rev-list count between tags
                GitCommandResult revCountResult = gitClient.execute(
                        repository.getLocalPath(),
                        List.of("rev-list", "--count", prev.name() + ".." + current.name())
                );
                if (revCountResult.isSuccessful() && !revCountResult.standardOutput().isBlank()) {
                    try {
                        commitsCount = Long.parseLong(revCountResult.standardOutput().trim());
                    } catch (NumberFormatException ignored) {}
                }

                // Churn between tags
                GitCommandResult diffStatResult = gitClient.execute(
                        repository.getLocalPath(),
                        List.of("diff", "--shortstat", prev.name() + ".." + current.name())
                );
                if (diffStatResult.isSuccessful() && !diffStatResult.standardOutput().isBlank()) {
                    churn = parseShortStatChurn(diffStatResult.standardOutput().trim());
                }
            } else {
                // First tag: commits leading to it
                GitCommandResult revCountResult = gitClient.execute(
                        repository.getLocalPath(),
                        List.of("rev-list", "--count", current.name())
                );
                if (revCountResult.isSuccessful() && !revCountResult.standardOutput().isBlank()) {
                    try {
                        commitsCount = Long.parseLong(revCountResult.standardOutput().trim());
                    } catch (NumberFormatException ignored) {}
                }
            }

            releases.add(new ReleaseMetrics(
                    current.name(),
                    current.date(),
                    current.hash(),
                    current.subject(),
                    daysSincePrevious,
                    commitsCount,
                    churn
            ));
        }

        // Return latest releases first
        Collections.reverse(releases);

        double avgInterval = intervalCount > 0 ? (double) totalIntervalDays / intervalCount : 0.0;
        avgInterval = Math.round(avgInterval * 10.0) / 10.0;

        return new ReleaseAnalyticsResponse(releases.size(), avgInterval, releases);
    }

    private long parseShortStatChurn(String shortstat) {
        long additions = 0;
        long deletions = 0;
        java.util.regex.Matcher addMatcher = java.util.regex.Pattern.compile("(\\d+) insertion").matcher(shortstat);
        if (addMatcher.find()) {
            additions = Long.parseLong(addMatcher.group(1));
        }
        java.util.regex.Matcher delMatcher = java.util.regex.Pattern.compile("(\\d+) deletion").matcher(shortstat);
        if (delMatcher.find()) {
            deletions = Long.parseLong(delMatcher.group(1));
        }
        return additions + deletions;
    }
}
