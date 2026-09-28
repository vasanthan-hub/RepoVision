package com.repovision.commit.application;

import com.repovision.commit.domain.FileChange;

import java.util.List;
import java.util.UUID;

public interface FileChangeStore {

    List<FileChange> saveAll(
            List<FileChange> fileChanges
    );

    List<FileChange> findByCommitId(
            UUID commitId
    );

    List<FileChange> findByCommitIds(
            List<UUID> commitIds
    );

    List<FileChange> findByRepositoryId(
            UUID repositoryId
    );
}