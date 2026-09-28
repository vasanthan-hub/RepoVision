package com.repovision.commit.infrastructure.persistence;

import com.repovision.commit.application.FileChangeStore;
import com.repovision.commit.domain.FileChange;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JpaFileChangeStore
        implements FileChangeStore {

    private final FileChangeJpaRepository fileChangeJpaRepository;
    private final FileChangeMapper fileChangeMapper;

    public JpaFileChangeStore(
            FileChangeJpaRepository fileChangeJpaRepository,
            FileChangeMapper fileChangeMapper
    ) {
        this.fileChangeJpaRepository =
                fileChangeJpaRepository;

        this.fileChangeMapper =
                fileChangeMapper;
    }

    @Override
    public List<FileChange> saveAll(
            List<FileChange> fileChanges
    ) {

        if (fileChanges.isEmpty()) {
            return List.of();
        }

        /*
         * Get all commit IDs involved in this batch.
         */
        List<UUID> commitIds =
                fileChanges.stream()
                        .map(FileChange::getCommitId)
                        .distinct()
                        .toList();

        /*
         * Load existing file changes for those commits
         * with one database query.
         */
        List<FileChangeEntity> existingEntities =
                fileChangeJpaRepository
                        .findByCommitIdIn(commitIds);

        /*
         * Create a lookup map using:
         *
         * commitId + filePath
         *
         * as the business key.
         */
        Map<String, FileChangeEntity> existingByKey =
                existingEntities.stream()
                        .collect(
                                Collectors.toMap(
                                        entity ->
                                                createKey(
                                                        entity.getCommitId(),
                                                        entity.getFilePath()
                                                ),
                                        Function.identity()
                                )
                        );

        Map<String, FileChangeEntity> entitiesToSaveByKey =
                new LinkedHashMap<>();

        /*
         * Process every file change from Git.
         */
        for (FileChange fileChange : fileChanges) {

            String key =
                    createKey(
                            fileChange.getCommitId(),
                            fileChange.getFilePath()
                    );

            FileChangeEntity existingEntity =
                    existingByKey.get(key);

            /*
             * No existing record in database:
             * create or update a new database row in batch.
             */
            if (existingEntity == null) {

                FileChangeEntity pendingEntity =
                        entitiesToSaveByKey.get(key);

                if (pendingEntity == null) {
                    entitiesToSaveByKey.put(
                            key,
                            fileChangeMapper.toEntity(fileChange)
                    );
                } else {
                    pendingEntity.setAdditions(
                            fileChange.getAdditions()
                    );
                    pendingEntity.setDeletions(
                            fileChange.getDeletions()
                    );
                }

            } else {

                /*
                 * Existing record:
                 * update its statistics.
                 */
                existingEntity.setAdditions(
                        fileChange.getAdditions()
                );

                existingEntity.setDeletions(
                        fileChange.getDeletions()
                );

                entitiesToSaveByKey.put(
                        key,
                        existingEntity
                );
            }
        }

        /*
         * Insert new records and update
         * existing records.
         */
        List<FileChangeEntity> savedEntities =
                fileChangeJpaRepository.saveAll(
                        entitiesToSaveByKey.values()
                );

        /*
         * Convert database entities back
         * into domain objects.
         */
        return savedEntities.stream()
                .map(fileChangeMapper::toDomain)
                .toList();
    }

    @Override
    public List<FileChange> findByCommitId(
            UUID commitId
    ) {

        return fileChangeJpaRepository
                .findByCommitId(commitId)
                .stream()
                .map(fileChangeMapper::toDomain)
                .toList();
    }

    @Override
    public List<FileChange> findByCommitIds(
            List<UUID> commitIds
    ) {

        if (commitIds.isEmpty()) {
            return List.of();
        }

        return fileChangeJpaRepository
                .findByCommitIdIn(commitIds)
                .stream()
                .map(fileChangeMapper::toDomain)
                .toList();
    }

    @Override
    public List<FileChange> findByRepositoryId(
            UUID repositoryId
    ) {
        return fileChangeJpaRepository
                .findByRepositoryId(repositoryId)
                .stream()
                .map(fileChangeMapper::toDomain)
                .toList();
    }

    /*
     * Creates the business key for a file change.
     *
     * Same commit + same file = same file change.
     */
    private String createKey(
            UUID commitId,
            String filePath
    ) {
        return commitId + ":" + filePath;
    }
}