package com.repovision.analysis.api.dto;

import java.time.Instant;

public record FileCommitSummary(
        String hash,
        String authorName,
        Instant authoredAt,
        String subject,
        long additions,
        long deletions
) {
}
