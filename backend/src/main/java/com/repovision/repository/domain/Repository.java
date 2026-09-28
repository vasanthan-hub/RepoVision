package com.repovision.repository.domain;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Repository {

    private final UUID id;
    private String name;
    private Path localPath;
    private String defaultBranch;
    private AnalysisStatus analysisStatus;
    private final Instant createdAt;
    private Instant updatedAt;


    public Repository(
            UUID id,
            String name,
            Path localPath,
            String defaultBranch
    ) {
        this(
                id,
                name,
                localPath,
                defaultBranch,
                AnalysisStatus.NOT_ANALYZED,
                Instant.now(),
                Instant.now()
        );
    }


    private Repository(
            UUID id,
            String name,
            Path localPath,
            String defaultBranch,
            AnalysisStatus analysisStatus,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(
                id,
                "Repository id must not be null"
        );

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Repository name must not be blank"
            );
        }

        this.name = name;

        this.localPath = Objects.requireNonNull(
                localPath,
                "Repository path must not be null"
        );

        if (defaultBranch == null || defaultBranch.isBlank()) {
            throw new IllegalArgumentException(
                    "Default branch must not be blank"
            );
        }

        this.defaultBranch = defaultBranch;

        this.analysisStatus = Objects.requireNonNull(
                analysisStatus,
                "Analysis status must not be null"
        );

        this.createdAt = Objects.requireNonNull(
                createdAt,
                "Created time must not be null"
        );

        this.updatedAt = Objects.requireNonNull(
                updatedAt,
                "Updated time must not be null"
        );
    }


    public static Repository reconstitute(
            UUID id,
            String name,
            Path localPath,
            String defaultBranch,
            AnalysisStatus analysisStatus,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Repository(
                id,
                name,
                localPath,
                defaultBranch,
                analysisStatus,
                createdAt,
                updatedAt
        );
    }


    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Path getLocalPath() {
        return localPath;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public AnalysisStatus getAnalysisStatus() {
        return analysisStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void rename(String name) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Repository name must not be blank"
            );
        }

        this.name = name;
        touch();
    }

    public void changePath(Path localPath) {

        this.localPath = Objects.requireNonNull(
                localPath,
                "Repository path must not be null"
        );

        touch();
    }

    public void changeDefaultBranch(String defaultBranch) {

        if (defaultBranch == null || defaultBranch.isBlank()) {
            throw new IllegalArgumentException(
                    "Default branch must not be blank"
            );
        }

        this.defaultBranch = defaultBranch;
        touch();
    }


    public void startAnalysis() {

        if (analysisStatus == AnalysisStatus.ANALYZING) {
            throw new IllegalStateException(
                    "Repository analysis is already in progress"
            );
        }

        this.analysisStatus = AnalysisStatus.ANALYZING;
        touch();
    }

    public void markAnalysisCompleted() {

        if (analysisStatus != AnalysisStatus.ANALYZING) {
            throw new IllegalStateException(
                    "Repository analysis is not currently in progress"
            );
        }

        this.analysisStatus = AnalysisStatus.COMPLETED;
        touch();
    }

    public void markAnalysisFailed() {

        if (analysisStatus != AnalysisStatus.ANALYZING) {
            throw new IllegalStateException(
                    "Repository analysis is not currently in progress"
            );
        }

        this.analysisStatus = AnalysisStatus.FAILED;
        touch();
    }



    private void touch() {
        this.updatedAt = Instant.now();
    }
}