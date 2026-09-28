package com.repovision.repository.infrastructure.persistence;

import com.repovision.repository.domain.Repository;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class RepositoryMapper {

    public RepositoryEntity toEntity(Repository repository) {

        RepositoryEntity entity = new RepositoryEntity();

        entity.setId(repository.getId());
        entity.setName(repository.getName());
        entity.setLocalPath(repository.getLocalPath().toString());
        entity.setDefaultBranch(repository.getDefaultBranch());
        entity.setAnalysisStatus(repository.getAnalysisStatus());
        entity.setCreatedAt(repository.getCreatedAt());
        entity.setUpdatedAt(repository.getUpdatedAt());

        return entity;
    }

    public Repository toDomain(RepositoryEntity entity) {

        return Repository.reconstitute(
                entity.getId(),
                entity.getName(),
                Path.of(entity.getLocalPath()),
                entity.getDefaultBranch(),
                entity.getAnalysisStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}