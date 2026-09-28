package com.repovision.commit.application;

import com.repovision.commit.domain.Commit;

import java.util.List;
import java.util.UUID;

public interface CommitStore {

    List<Commit> saveAll(List<Commit> commits);
    long countByRepositoryId(UUID repositoryId);
    List<Commit> findByRepositoryId(UUID repositoryId);

}