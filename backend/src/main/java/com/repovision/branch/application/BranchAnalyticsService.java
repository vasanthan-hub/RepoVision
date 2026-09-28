package com.repovision.branch.application;

import com.repovision.branch.api.dto.BranchAnalyticsResponse;
import com.repovision.branch.api.dto.BranchMetrics;
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
public class BranchAnalyticsService {

    private static final long DEFAULT_STALE_THRESHOLD_DAYS = 30;

    private final RepositoryStore repositoryStore;
    private final GitClient gitClient;

    public BranchAnalyticsService(
            RepositoryStore repositoryStore,
            GitClient gitClient
    ) {
        this.repositoryStore = repositoryStore;
        this.gitClient = gitClient;
    }

    public BranchAnalyticsResponse getBranchAnalytics(UUID repositoryId) {
        return getBranchAnalytics(repositoryId, DEFAULT_STALE_THRESHOLD_DAYS);
    }

    public BranchAnalyticsResponse getBranchAnalytics(UUID repositoryId, long staleDaysThreshold) {
        Repository repository = repositoryStore.findById(repositoryId)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found with id: " + repositoryId));

        String defaultBranch = repository.getDefaultBranch() != null ? repository.getDefaultBranch() : "main";

        // 1. Get all branches with details
        GitCommandResult branchListResult = gitClient.execute(
                repository.getLocalPath(),
                List.of(
                        "branch",
                        "-a",
                        "--format=%(refname:short)%09%(objectname)%09%(authordate:iso-strict)%09%(authorname)%09%(subject)"
                )
        );

        if (!branchListResult.isSuccessful() || branchListResult.standardOutput().isBlank()) {
            return new BranchAnalyticsResponse(0, 0, 0, List.of());
        }

        // 2. Get merged branches into default branch
        Set<String> mergedBranches = new HashSet<>();
        GitCommandResult mergedResult = gitClient.execute(
                repository.getLocalPath(),
                List.of(
                        "branch",
                        "-a",
                        "--merged",
                        defaultBranch,
                        "--format=%(refname:short)"
                )
        );
        if (mergedResult.isSuccessful()) {
            for (String line : mergedResult.standardOutput().split("\\R")) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    mergedBranches.add(trimmed);
                }
            }
        }

        Instant now = Instant.now();
        Map<String, BranchMetrics> branchMetricsMap = new LinkedHashMap<>();

        for (String line : branchListResult.standardOutput().split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.contains("->") || trimmed.contains("origin/HEAD")) {
                continue;
            }

            String[] parts = trimmed.split("\t", -1);
            if (parts.length < 5) {
                continue;
            }

            String rawName = parts[0].trim();
            String hash = parts[1].trim();
            String dateStr = parts[2].trim();
            String author = parts[3].trim();
            String subject = parts[4].trim();

            Instant lastActivity;
            try {
                lastActivity = OffsetDateTime.parse(dateStr).toInstant();
            } catch (DateTimeParseException e) {
                lastActivity = now;
            }

            long daysInactive = Math.max(0, Duration.between(lastActivity, now).toDays());
            boolean isDefault = rawName.equals(defaultBranch) || rawName.equals("origin/" + defaultBranch);
            boolean isMerged = mergedBranches.contains(rawName);
            boolean isStale = !isDefault && daysInactive > staleDaysThreshold;

            // Commit count on this branch
            long commitCount = 0;
            GitCommandResult countResult = gitClient.execute(
                    repository.getLocalPath(),
                    List.of("rev-list", "--count", rawName)
            );
            if (countResult.isSuccessful() && !countResult.standardOutput().isBlank()) {
                try {
                    commitCount = Long.parseLong(countResult.standardOutput().trim());
                } catch (NumberFormatException ignored) {}
            }

            // Deduplicate local vs remote tracking (favor local branch if exists)
            String normalizedKey = rawName.startsWith("origin/") ? rawName.substring(7) : rawName;
            if (rawName.startsWith("origin/") && branchMetricsMap.containsKey(normalizedKey)) {
                // local already recorded, keep local
                continue;
            }

            branchMetricsMap.put(normalizedKey, new BranchMetrics(
                    rawName,
                    hash,
                    subject,
                    author,
                    lastActivity,
                    commitCount,
                    isStale,
                    isMerged,
                    isDefault,
                    daysInactive
            ));
        }

        List<BranchMetrics> branches = new ArrayList<>(branchMetricsMap.values());
        // Sort: default branch first, then active branches by most recent, then stale branches
        branches.sort((a, b) -> {
            if (a.isDefault()) return -1;
            if (b.isDefault()) return 1;
            if (a.isStale() != b.isStale()) {
                return a.isStale() ? 1 : -1;
            }
            return b.lastActivity().compareTo(a.lastActivity());
        });

        int staleCount = (int) branches.stream().filter(BranchMetrics::isStale).count();
        int activeCount = branches.size() - staleCount;

        return new BranchAnalyticsResponse(branches.size(), activeCount, staleCount, branches);
    }
}
