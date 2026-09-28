package com.repovision.commit.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "commits",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_commit_repository_hash",
                        columnNames = {
                                "repository_id",
                                "hash"
                        }
                )
        },
        indexes = {
                @Index(name = "idx_commits_repository_id", columnList = "repository_id"),
                @Index(name = "idx_commits_repo_authored", columnList = "repository_id, authored_at")
        }
)
public class CommitEntity {

    @Id
    private UUID id;

    private UUID repositoryId;
    private String hash;
    private String authorName;
    private String authorEmail;
    private Instant authoredAt;
    private String subject;

    private long additions;
    private long deletions;

    protected CommitEntity() {
        // Required by JPA
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(UUID repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public Instant getAuthoredAt() {
        return authoredAt;
    }

    public void setAuthoredAt(Instant authoredAt) {
        this.authoredAt = authoredAt;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public long getAdditions() {
        return additions;
    }

    public void setAdditions(long additions) {
        this.additions = additions;
    }

    public long getDeletions() {
        return deletions;
    }

    public void setDeletions(long deletions) {
        this.deletions = deletions;
    }
}