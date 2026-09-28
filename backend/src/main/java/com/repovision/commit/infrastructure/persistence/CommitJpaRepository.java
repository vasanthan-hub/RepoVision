package com.repovision.commit.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommitJpaRepository
        extends JpaRepository<CommitEntity, UUID> {

    List<CommitEntity> findByRepositoryIdAndHashIn(
            UUID repositoryId,
            List<String> hashes
    );

    long countByRepositoryId(UUID repositoryId);
    List<CommitEntity> findByRepositoryId(UUID repositoryId);
}