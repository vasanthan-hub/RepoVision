package com.repovision.branch.api.dto;

import java.time.Instant;

public record BranchMetrics(
        String name,
        String latestCommitHash,
        String latestCommitSubject,
        String latestCommitAuthor,
        Instant lastActivity,
        long commitCount,
        boolean isStale,
        boolean isMerged,
        boolean isDefault,
        long daysInactive
) {
}
