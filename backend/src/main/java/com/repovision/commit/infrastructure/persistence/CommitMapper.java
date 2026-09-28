package com.repovision.commit.infrastructure.persistence;

import com.repovision.commit.domain.Commit;
import org.springframework.stereotype.Component;

@Component
public class CommitMapper {

    public CommitEntity toEntity(Commit commit) {

        CommitEntity entity =
                new CommitEntity();

        entity.setId(commit.getId());
        entity.setRepositoryId(
                commit.getRepositoryId()
        );
        entity.setHash(
                commit.getHash()
        );
        entity.setAuthorName(
                commit.getAuthorName()
        );
        entity.setAuthorEmail(
                commit.getAuthorEmail()
        );
        entity.setAuthoredAt(
                commit.getAuthoredAt()
        );
        entity.setSubject(
                commit.getSubject()
        );
        entity.setAdditions(
                commit.getAdditions()
        );
        entity.setDeletions(
                commit.getDeletions()
        );

        return entity;
    }

    public Commit toDomain(CommitEntity entity) {

        return new Commit(
                entity.getId(),
                entity.getRepositoryId(),
                entity.getHash(),
                entity.getAuthorName(),
                entity.getAuthorEmail(),
                entity.getAuthoredAt(),
                entity.getSubject(),
                entity.getAdditions(),
                entity.getDeletions()
        );
    }
}