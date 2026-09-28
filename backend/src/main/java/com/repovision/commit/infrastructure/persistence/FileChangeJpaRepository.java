package com.repovision.commit.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FileChangeJpaRepository
        extends JpaRepository<FileChangeEntity, UUID> {

    List<FileChangeEntity> findByCommitId(
            UUID commitId
    );

    List<FileChangeEntity> findByCommitIdIn(
            List<UUID> commitIds
    );

    @org.springframework.data.jpa.repository.Query("SELECT fc FROM FileChangeEntity fc JOIN CommitEntity c ON fc.commitId = c.id WHERE c.repositoryId = :repositoryId")
    List<FileChangeEntity> findByRepositoryId(
            @org.springframework.data.repository.query.Param("repositoryId") UUID repositoryId
    );

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(fc) FROM FileChangeEntity fc JOIN CommitEntity c ON fc.commitId = c.id WHERE c.repositoryId = :repositoryId")
    long countByRepositoryId(
            @org.springframework.data.repository.query.Param("repositoryId") UUID repositoryId
    );
}