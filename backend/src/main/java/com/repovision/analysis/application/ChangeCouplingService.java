package com.repovision.analysis.application;

import com.repovision.analysis.api.dto.ChangeCouplingResponse;
import com.repovision.analysis.api.dto.FileCoupling;
import com.repovision.commit.application.FileChangeStore;
import com.repovision.commit.domain.FileChange;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChangeCouplingService {

    private final FileChangeStore fileChangeStore;

    public ChangeCouplingService(FileChangeStore fileChangeStore) {
        this.fileChangeStore = fileChangeStore;
    }

    public ChangeCouplingResponse getChangeCoupling(UUID repositoryId) {
        List<FileCoupling> couplings = computeCoupledFiles(repositoryId);
        int strongCount = (int) couplings.stream()
                .filter(c -> "STRONG".equals(c.strength()))
                .count();

        return new ChangeCouplingResponse(couplings.size(), strongCount, couplings);
    }

    public List<FileCoupling> computeCoupledFiles(UUID repositoryId) {
        List<FileChange> allChanges = fileChangeStore.findByRepositoryId(repositoryId);
        if (allChanges.isEmpty()) {
            return List.of();
        }

        // Group files by commit
        Map<UUID, Set<String>> filesByCommit = new HashMap<>();
        Map<String, Integer> fileTotalCommitCounts = new HashMap<>();

        for (FileChange fc : allChanges) {
            String path = fc.getFilePath();
            filesByCommit.computeIfAbsent(fc.getCommitId(), k -> new HashSet<>()).add(path);
            fileTotalCommitCounts.merge(path, 1, Integer::sum);
        }

        // Co-change occurrences: only analyze commits that change between 2 and 25 files
        Map<String, Integer> coChangePairs = new HashMap<>();

        for (Set<String> files : filesByCommit.values()) {
            if (files.size() < 2 || files.size() > 25) {
                continue;
            }

            List<String> sortedFiles = new ArrayList<>(files);
            Collections.sort(sortedFiles);

            for (int i = 0; i < sortedFiles.size(); i++) {
                for (int j = i + 1; j < sortedFiles.size(); j++) {
                    String pairKey = sortedFiles.get(i) + "\t" + sortedFiles.get(j);
                    coChangePairs.merge(pairKey, 1, Integer::sum);
                }
            }
        }

        List<FileCoupling> result = new ArrayList<>();
        // Minimum co-changes threshold: 2 (or 1 if total commits is small)
        int minCoChanges = filesByCommit.size() < 10 ? 1 : 2;

        for (Map.Entry<String, Integer> entry : coChangePairs.entrySet()) {
            int coChanges = entry.getValue();
            if (coChanges < minCoChanges) {
                continue;
            }

            String[] files = entry.getKey().split("\t", -1);
            String fileA = files[0];
            String fileB = files[1];

            int countA = fileTotalCommitCounts.getOrDefault(fileA, coChanges);
            int countB = fileTotalCommitCounts.getOrDefault(fileB, coChanges);

            int minSingle = Math.min(countA, countB);
            double percentage = minSingle > 0 ? (double) coChanges / minSingle * 100.0 : 0.0;
            double roundedPct = Math.round(percentage * 10.0) / 10.0;

            String strength;
            if (roundedPct >= 75.0) {
                strength = "STRONG";
            } else if (roundedPct >= 50.0) {
                strength = "MODERATE";
            } else {
                strength = "WEAK";
            }

            result.add(new FileCoupling(fileA, fileB, coChanges, countA, countB, roundedPct, strength));
        }

        result.sort((a, b) -> {
            int cmp = Double.compare(b.couplingPercentage(), a.couplingPercentage());
            if (cmp != 0) return cmp;
            return Integer.compare(b.coChangeCount(), a.coChangeCount());
        });

        return result;
    }
}
