package com.repovision.commit.infrastructure.persistence;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
        name = "file_changes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_file_change_commit_path",
                        columnNames = {
                                "commit_id",
                                "file_path"
                        }
                )
        },
        indexes = {
                @Index(name = "idx_file_changes_commit_id", columnList = "commit_id"),
                @Index(name = "idx_file_changes_file_path", columnList = "file_path")
        }
)
public class FileChangeEntity {

    @Id
    private UUID id;

    private UUID commitId;

    private String filePath;

    private long additions;

    private long deletions;

    protected FileChangeEntity() {
        // Required by JPA
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCommitId() {
        return commitId;
    }

    public void setCommitId(UUID commitId) {
        this.commitId = commitId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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