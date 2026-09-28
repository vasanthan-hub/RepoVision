package com.repovision.commit.domain;

import java.util.Objects;
import java.util.UUID;

public class FileChange {

    private final UUID id;
    private final UUID commitId;
    private final String filePath;
    private final long additions;
    private final long deletions;

    public FileChange(
            UUID id,
            UUID commitId,
            String filePath,
            long additions,
            long deletions
    ) {

        this.id = Objects.requireNonNull(
                id,
                "File change id must not be null"
        );

        this.commitId = Objects.requireNonNull(
                commitId,
                "Commit id must not be null"
        );

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException(
                    "File path must not be blank"
            );
        }

        this.filePath = filePath;

        if (additions < 0) {
            throw new IllegalArgumentException(
                    "File additions must not be negative"
            );
        }

        if (deletions < 0) {
            throw new IllegalArgumentException(
                    "File deletions must not be negative"
            );
        }

        this.additions = additions;
        this.deletions = deletions;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCommitId() {
        return commitId;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getAdditions() {
        return additions;
    }

    public long getDeletions() {
        return deletions;
    }
}