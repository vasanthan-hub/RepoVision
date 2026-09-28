package com.repovision.commit.api.dto;

import com.repovision.commit.domain.Commit;

import java.time.Instant;
import java.util.UUID;

public record CommitResponse(
        UUID id,
        UUID repositoryId,
        String hash,
        String authorName,
        String authorEmail,
        Instant authoredAt,
        String subject,
        long additions,
        long deletions
) {

    public static CommitResponse from(
            Commit commit
    ) {
        return new CommitResponse(
                commit.getId(),
                commit.getRepositoryId(),
                commit.getHash(),
                commit.getAuthorName(),
                commit.getAuthorEmail(),
                commit.getAuthoredAt(),
                commit.getSubject(),
                commit.getAdditions(),
                commit.getDeletions()
        );
    }
}