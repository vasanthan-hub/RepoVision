package com.repovision.commit.infrastructure.persistence;

import com.repovision.commit.domain.FileChange;
import org.springframework.stereotype.Component;

@Component
public class FileChangeMapper {

    public FileChangeEntity toEntity(
            FileChange fileChange
    ) {

        FileChangeEntity entity =
                new FileChangeEntity();

        entity.setId(
                fileChange.getId()
        );

        entity.setCommitId(
                fileChange.getCommitId()
        );

        entity.setFilePath(
                fileChange.getFilePath()
        );

        entity.setAdditions(
                fileChange.getAdditions()
        );

        entity.setDeletions(
                fileChange.getDeletions()
        );

        return entity;
    }

    public FileChange toDomain(
            FileChangeEntity entity
    ) {

        return new FileChange(
                entity.getId(),
                entity.getCommitId(),
                entity.getFilePath(),
                entity.getAdditions(),
                entity.getDeletions()
        );
    }
}