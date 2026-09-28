package com.repovision.repository.api.dto;

import com.repovision.repository.domain.AnalysisStatus;
import com.repovision.repository.domain.Repository;

import java.time.Instant;
import java.util.UUID;

public record RepositoryResponse(
        UUID id,
        String name,
        String localPath,
        String defaultBranch,
        AnalysisStatus analysisStatus,
        Instant createdAt,
        Instant updatedAt
) {

    public static RepositoryResponse from(
            Repository repository
    ) {
        return new RepositoryResponse(
                repository.getId(),
                repository.getName(),
                repository.getLocalPath().toString(),
                repository.getDefaultBranch(),
                repository.getAnalysisStatus(),
                repository.getCreatedAt(),
                repository.getUpdatedAt()
        );
    }
}