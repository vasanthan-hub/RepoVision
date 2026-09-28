package com.repovision.analysis.application;

import com.repovision.analysis.api.dto.*;
import com.repovision.commit.application.CommitStore;
import com.repovision.commit.application.FileChangeStore;
import com.repovision.commit.domain.Commit;
import com.repovision.commit.domain.FileChange;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FileAnalyticsService {

    private final CommitStore commitStore;
    private final FileChangeStore fileChangeStore;

    public FileAnalyticsService(
            CommitStore commitStore,
            FileChangeStore fileChangeStore
    ) {
        this.commitStore = commitStore;
        this.fileChangeStore = fileChangeStore;
    }

    public FileAnalyticsResponse getFileAnalytics(UUID repositoryId) {
        List<FileMetrics> fileMetricsList = computeFileMetrics(repositoryId);
        return new FileAnalyticsResponse(fileMetricsList.size(), fileMetricsList);
    }

    public List<FileMetrics> getHotspots(UUID repositoryId, int limit) {
        List<FileMetrics> fileMetricsList = computeFileMetrics(repositoryId);
        return fileMetricsList.stream()
                .sorted(Comparator.comparingDouble(FileMetrics::hotspotScore).reversed())
                .limit(limit > 0 ? limit : 10)
                .toList();
    }

    public FileDetailResponse getFileDetail(UUID repositoryId, String filePath) {
        List<Commit> commits = commitStore.findByRepositoryId(repositoryId);
        Map<UUID, Commit> commitMap = commits.stream()
                .collect(Collectors.toMap(Commit::getId, Function.identity(), (a, b) -> a));

        List<FileChange> changes = fileChangeStore.findByRepositoryId(repositoryId).stream()
                .filter(fc -> fc.getFilePath().equals(filePath))
                .toList();

        if (changes.isEmpty()) {
            throw new IllegalArgumentException("File not found in repository: " + filePath);
        }

        long modificationCount = changes.size();
        long totalAdditions = changes.stream().mapToLong(FileChange::getAdditions).sum();
        long totalDeletions = changes.stream().mapToLong(FileChange::getDeletions).sum();
        long totalChurn = totalAdditions + totalDeletions;

        List<Commit> fileCommits = changes.stream()
                .map(fc -> commitMap.get(fc.getCommitId()))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Commit::getAuthoredAt))
                .toList();

        Instant firstModified = fileCommits.isEmpty() ? null : fileCommits.get(0).getAuthoredAt();
        Instant lastModified = fileCommits.isEmpty() ? null : fileCommits.get(fileCommits.size() - 1).getAuthoredAt();

        // Calculate contributors for this file
        Map<String, List<Commit>> commitsByAuthor = fileCommits.stream()
                .collect(Collectors.groupingBy(Commit::getAuthorEmail));

        Map<UUID, FileChange> changeByCommitId = changes.stream()
                .collect(Collectors.toMap(FileChange::getCommitId, Function.identity(), (a, b) -> a));

        List<FileContributor> contributors = commitsByAuthor.entrySet().stream()
                .map(entry -> {
                    String authorEmail = entry.getKey();
                    List<Commit> authorCommits = entry.getValue();
                    String authorName = authorCommits.get(0).getAuthorName();
                    long commitCount = authorCommits.size();

                    long authorChurn = authorCommits.stream()
                            .map(c -> changeByCommitId.get(c.getId()))
                            .filter(Objects::nonNull)
                            .mapToLong(fc -> fc.getAdditions() + fc.getDeletions())
                            .sum();

                    double percentage = totalChurn > 0
                            ? (double) authorChurn / totalChurn * 100.0
                            : (modificationCount > 0 ? (double) commitCount / modificationCount * 100.0 : 0.0);

                    return new FileContributor(authorName, authorEmail, commitCount, authorChurn, Math.round(percentage * 10.0) / 10.0);
                })
                .sorted(Comparator.comparingDouble(FileContributor::percentage).reversed())
                .toList();

        List<FileCommitSummary> recentCommits = changes.stream()
                .map(fc -> {
                    Commit c = commitMap.get(fc.getCommitId());
                    if (c == null) return null;
                    return new FileCommitSummary(c.getHash(), c.getAuthorName(), c.getAuthoredAt(), c.getSubject(), fc.getAdditions(), fc.getDeletions());
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(FileCommitSummary::authoredAt).reversed())
                .limit(20)
                .toList();

        // Compute hotspot score for this single file in context of repository
        List<FileMetrics> allMetrics = computeFileMetrics(repositoryId);
        double hotspotScore = allMetrics.stream()
                .filter(m -> m.filePath().equals(filePath))
                .findFirst()
                .map(FileMetrics::hotspotScore)
                .orElse(0.0);

        double averageChangeSize = modificationCount > 0 ? (double) totalChurn / modificationCount : 0.0;

        return new FileDetailResponse(
                filePath,
                modificationCount,
                totalAdditions,
                totalDeletions,
                totalChurn,
                contributors.size(),
                firstModified,
                lastModified,
                Math.round(averageChangeSize * 10.0) / 10.0,
                hotspotScore,
                contributors,
                recentCommits
        );
    }

    public List<FileMetrics> computeFileMetrics(UUID repositoryId) {
        List<Commit> commits = commitStore.findByRepositoryId(repositoryId);
        if (commits.isEmpty()) {
            return List.of();
        }

        Map<UUID, Commit> commitMap = commits.stream()
                .collect(Collectors.toMap(Commit::getId, Function.identity(), (a, b) -> a));

        List<FileChange> allChanges = fileChangeStore.findByRepositoryId(repositoryId);
        if (allChanges.isEmpty()) {
            return List.of();
        }

        Map<String, List<FileChange>> changesByFile = allChanges.stream()
                .collect(Collectors.groupingBy(FileChange::getFilePath));

        record RawFileFact(
                String path,
                long modCount,
                long additions,
                long deletions,
                long churn,
                int contributorCount,
                Instant firstModified,
                Instant lastModified
        ) {}

        List<RawFileFact> rawFacts = new ArrayList<>();
        Instant now = Instant.now();

        long maxModCount = 0;
        long maxChurn = 0;
        int maxContributors = 0;

        for (Map.Entry<String, List<FileChange>> entry : changesByFile.entrySet()) {
            String path = entry.getKey();
            List<FileChange> fileChanges = entry.getValue();

            long modCount = fileChanges.size();
            long additions = fileChanges.stream().mapToLong(FileChange::getAdditions).sum();
            long deletions = fileChanges.stream().mapToLong(FileChange::getDeletions).sum();
            long churn = additions + deletions;

            Set<String> contributors = new HashSet<>();
            Instant first = null;
            Instant last = null;

            for (FileChange fc : fileChanges) {
                Commit c = commitMap.get(fc.getCommitId());
                if (c != null) {
                    contributors.add(c.getAuthorEmail() != null ? c.getAuthorEmail() : c.getAuthorName());
                    Instant authoredAt = c.getAuthoredAt();
                    if (authoredAt != null) {
                        if (first == null || authoredAt.isBefore(first)) {
                            first = authoredAt;
                        }
                        if (last == null || authoredAt.isAfter(last)) {
                            last = authoredAt;
                        }
                    }
                }
            }

            int contribCount = contributors.isEmpty() ? 1 : contributors.size();

            if (modCount > maxModCount) maxModCount = modCount;
            if (churn > maxChurn) maxChurn = churn;
            if (contribCount > maxContributors) maxContributors = contribCount;

            rawFacts.add(new RawFileFact(path, modCount, additions, deletions, churn, contribCount, first, last));
        }

        // Compute scores
        List<FileMetrics> result = new ArrayList<>();
        for (RawFileFact fact : rawFacts) {
            double normMod = maxModCount > 0 ? (double) fact.modCount() / maxModCount : 0.0;
            double normChurn = maxChurn > 0 ? Math.log1p(fact.churn()) / Math.log1p(maxChurn) : 0.0;
            double normContrib = maxContributors > 0 ? (double) fact.contributorCount() / maxContributors : 0.0;

            double recency = 0.5; // default if no date
            if (fact.lastModified() != null) {
                long days = Math.max(0, Duration.between(fact.lastModified(), now).toDays());
                recency = Math.max(0.0, 1.0 - (days / 90.0));
            }

            // Documented formula:
            // Hotspot score (0-100) = (normMod * 0.40 + normChurn * 0.35 + recency * 0.15 + normContrib * 0.10) * 100
            double rawScore = (normMod * 0.40 + normChurn * 0.35 + recency * 0.15 + normContrib * 0.10) * 100.0;
            double hotspotScore = Math.round(rawScore * 10.0) / 10.0;
            double avgChangeSize = fact.modCount() > 0 ? (double) fact.churn() / fact.modCount() : 0.0;

            result.add(new FileMetrics(
                    fact.path(),
                    fact.modCount(),
                    fact.additions(),
                    fact.deletions(),
                    fact.churn(),
                    fact.contributorCount(),
                    fact.firstModified(),
                    fact.lastModified(),
                    Math.round(avgChangeSize * 10.0) / 10.0,
                    hotspotScore
            ));
        }

        return result.stream()
                .sorted(Comparator.comparingLong(FileMetrics::modificationCount).reversed())
                .toList();
    }
}
