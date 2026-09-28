package com.repovision.analysis.api.dto;

import java.time.Instant;
import java.util.List;

public record FileDetailResponse(
        String filePath,
        long modificationCount,
        long additions,
        long deletions,
        long churn,
        int contributorCount,
        Instant firstModified,
        Instant lastModified,
        double averageChangeSize,
        double hotspotScore,
        List<FileContributor> contributors,
        List<FileCommitSummary> recentCommits
) {
}
