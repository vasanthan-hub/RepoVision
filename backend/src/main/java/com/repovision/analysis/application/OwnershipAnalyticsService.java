package com.repovision.analysis.application;

import com.repovision.analysis.api.dto.*;
import com.repovision.commit.application.CommitStore;
import com.repovision.commit.application.FileChangeStore;
import com.repovision.commit.domain.Commit;
import com.repovision.commit.domain.FileChange;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OwnershipAnalyticsService {

    private final CommitStore commitStore;
    private final FileChangeStore fileChangeStore;

    public OwnershipAnalyticsService(
            CommitStore commitStore,
            FileChangeStore fileChangeStore
    ) {
        this.commitStore = commitStore;
        this.fileChangeStore = fileChangeStore;
    }

    public OwnershipAnalyticsResponse getOwnershipAnalytics(UUID repositoryId) {
        List<FileOwnership> ownerships = computeFileOwnerships(repositoryId);
        BusFactorResponse busFactor = computeBusFactor(repositoryId, ownerships);
        return new OwnershipAnalyticsResponse(ownerships.size(), busFactor, ownerships);
    }

    public BusFactorResponse getBusFactor(UUID repositoryId) {
        List<FileOwnership> ownerships = computeFileOwnerships(repositoryId);
        return computeBusFactor(repositoryId, ownerships);
    }

    public List<FileOwnership> computeFileOwnerships(UUID repositoryId) {
        List<Commit> commits = commitStore.findByRepositoryId(repositoryId);
        if (commits.isEmpty()) {
            return List.of();
        }

        Map<UUID, Commit> commitMap = commits.stream()
                .collect(Collectors.toMap(Commit::getId, Function.identity(), (a, b) -> a));

        List<FileChange> changes = fileChangeStore.findByRepositoryId(repositoryId);
        if (changes.isEmpty()) {
            return List.of();
        }

        Map<String, List<FileChange>> changesByFile = changes.stream()
                .collect(Collectors.groupingBy(FileChange::getFilePath));

        List<FileOwnership> result = new ArrayList<>();

        for (Map.Entry<String, List<FileChange>> entry : changesByFile.entrySet()) {
            String filePath = entry.getKey();
            List<FileChange> fileChanges = entry.getValue();

            String module = extractModule(filePath);
            long totalFileChurn = fileChanges.stream()
                    .mapToLong(fc -> fc.getAdditions() + fc.getDeletions())
                    .sum();

            // Group by author email
            Map<String, List<FileChange>> changesByAuthor = new HashMap<>();
            Map<String, String> authorNames = new HashMap<>();

            for (FileChange fc : fileChanges) {
                Commit c = commitMap.get(fc.getCommitId());
                if (c != null) {
                    String email = c.getAuthorEmail() != null ? c.getAuthorEmail() : "unknown";
                    authorNames.putIfAbsent(email, c.getAuthorName() != null ? c.getAuthorName() : email);
                    changesByAuthor.computeIfAbsent(email, k -> new ArrayList<>()).add(fc);
                }
            }

            List<FileContributor> contributors = new ArrayList<>();
            for (Map.Entry<String, List<FileChange>> authorEntry : changesByAuthor.entrySet()) {
                String email = authorEntry.getKey();
                String name = authorNames.get(email);
                List<FileChange> authorChanges = authorEntry.getValue();

                long authorChurn = authorChanges.stream()
                        .mapToLong(fc -> fc.getAdditions() + fc.getDeletions())
                        .sum();
                long authorCommits = authorChanges.size();

                double percentage = totalFileChurn > 0
                        ? (double) authorChurn / totalFileChurn * 100.0
                        : (fileChanges.isEmpty() ? 0.0 : (double) authorCommits / fileChanges.size() * 100.0);

                contributors.add(new FileContributor(
                        name,
                        email,
                        authorCommits,
                        authorChurn,
                        Math.round(percentage * 10.0) / 10.0
                ));
            }

            contributors.sort(Comparator.comparingDouble(FileContributor::percentage).reversed());

            FileContributor primary = contributors.isEmpty()
                    ? new FileContributor("None", "none", 0, 0, 0.0)
                    : contributors.get(0);

            String concentrationLevel;
            if (primary.percentage() >= 80.0) {
                concentrationLevel = "HIGH";
            } else if (primary.percentage() >= 50.0) {
                concentrationLevel = "MODERATE";
            } else {
                concentrationLevel = "BALANCED";
            }

            result.add(new FileOwnership(
                    filePath,
                    module,
                    primary.name(),
                    primary.email(),
                    primary.percentage(),
                    totalFileChurn,
                    contributors.size(),
                    concentrationLevel,
                    contributors
            ));
        }

        result.sort(Comparator.comparing(FileOwnership::filePath));
        return result;
    }

    private BusFactorResponse computeBusFactor(UUID repositoryId, List<FileOwnership> ownerships) {
        if (ownerships.isEmpty()) {
            return new BusFactorResponse(0, 0, 0, 0, List.of(), List.of("No file history available in repository."));
        }

        int totalFiles = ownerships.size();
        int singleContributorCount = (int) ownerships.stream()
                .filter(fo -> fo.contributorCount() == 1)
                .count();

        int highConcentrationCount = (int) ownerships.stream()
                .filter(fo -> "HIGH".equals(fo.concentrationLevel()))
                .count();

        // Group files by primary contributor (where primary owns >= 80%)
        Map<String, List<FileOwnership>> filesByPrimary = ownerships.stream()
                .filter(fo -> fo.primaryContributorPercentage() >= 70.0)
                .collect(Collectors.groupingBy(FileOwnership::primaryContributorEmail));

        Map<String, String> devNames = new HashMap<>();
        Map<String, Long> devChurn = new HashMap<>();

        for (FileOwnership fo : ownerships) {
            for (FileContributor c : fo.contributors()) {
                devNames.putIfAbsent(c.email(), c.name());
                devChurn.merge(c.email(), c.linesChanged(), Long::sum);
            }
        }

        List<DeveloperFileCount> devCounts = filesByPrimary.entrySet().stream()
                .map(entry -> {
                    String email = entry.getKey();
                    String name = devNames.getOrDefault(email, email);
                    int count = entry.getValue().size();
                    double pct = Math.round((double) count / totalFiles * 1000.0) / 10.0;
                    long churn = devChurn.getOrDefault(email, 0L);
                    return new DeveloperFileCount(name, email, count, pct, churn);
                })
                .sorted(Comparator.comparingInt(DeveloperFileCount::primarilyOwnedFilesCount).reversed())
                .toList();

        // Calculate bus factor: minimum developers whose departure leaves >= 50% files orphaned
        int busFactor = 0;
        int accumulatedFiles = 0;
        int targetThreshold = (int) Math.ceil(totalFiles * 0.50);

        for (DeveloperFileCount dev : devCounts) {
            busFactor++;
            accumulatedFiles += dev.primarilyOwnedFilesCount();
            if (accumulatedFiles >= targetThreshold) {
                break;
            }
        }

        if (busFactor == 0 && !devCounts.isEmpty()) {
            busFactor = 1;
        }

        List<String> evidence = new ArrayList<>();
        evidence.add(String.format("Estimated Bus Factor: %d key contributor(s) responsible for majority of files.", busFactor));
        evidence.add(String.format("%d of %d files (%.1f%%) have only a single contributor.",
                singleContributorCount, totalFiles, (double) singleContributorCount / totalFiles * 100.0));
        evidence.add(String.format("%d of %d files (%.1f%%) have high ownership concentration (>= 80%% by single author).",
                highConcentrationCount, totalFiles, (double) highConcentrationCount / totalFiles * 100.0));

        for (DeveloperFileCount dev : devCounts) {
            evidence.add(String.format("%s (%s) is primary contributor for %d files (%.1f%% of repository).",
                    dev.developerName(), dev.developerEmail(), dev.primarilyOwnedFilesCount(), dev.percentageOfCodebase()));
        }

        return new BusFactorResponse(
                busFactor,
                singleContributorCount,
                highConcentrationCount,
                totalFiles,
                devCounts,
                evidence
        );
    }

    private String extractModule(String filePath) {
        if (filePath == null || filePath.isBlank()) return "root";
        String normalized = filePath.replace('\\', '/').trim();
        if (normalized.contains("=>")) {
            normalized = com.repovision.commit.infrastructure.parser.CommitParser.resolveGitFilePath(normalized);
        }
        int lastSlash = normalized.lastIndexOf('/');
        if (lastSlash <= 0) {
            return "root";
        }
        return normalized.substring(0, lastSlash);
    }
}
