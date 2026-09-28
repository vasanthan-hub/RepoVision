package com.repovision.commit.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Commit {

    private final UUID id;
    private final UUID repositoryId;
    private final String hash;
    private final String authorName;
    private final String authorEmail;
    private final Instant authoredAt;
    private final String subject;
    private final long additions;
    private final long deletions;

    public Commit(
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
        this.id = Objects.requireNonNull(
                id,
                "Commit id must not be null"
        );

        this.repositoryId = Objects.requireNonNull(
                repositoryId,
                "Repository id must not be null"
        );
        if (additions < 0) {
            throw new IllegalArgumentException(
                    "Commit additions must not be negative"
            );
        }

        if (deletions < 0) {
            throw new IllegalArgumentException(
                    "Commit deletions must not be negative"
            );
        }
        this.additions = additions;
        this.deletions = deletions;

        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException(
                    "Commit hash must not be blank"
            );
        }

        this.hash = hash;

        if (authorName == null || authorName.isBlank()) {
            throw new IllegalArgumentException(
                    "Author name must not be blank"
            );
        }

        this.authorName = authorName;

        if (authorEmail == null || authorEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Author email must not be blank"
            );
        }

        this.authorEmail = authorEmail;

        this.authoredAt = Objects.requireNonNull(
                authoredAt,
                "Commit authored time must not be null"
        );

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "Commit subject must not be blank"
            );
        }

        this.subject = subject;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRepositoryId() {
        return repositoryId;
    }

    public String getHash() {
        return hash;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public Instant getAuthoredAt() {
        return authoredAt;
    }

    public String getSubject() {
        return subject;
    }
    public long getAdditions() {
        return additions;
    }

    public long getDeletions() {
        return deletions;
    }
}