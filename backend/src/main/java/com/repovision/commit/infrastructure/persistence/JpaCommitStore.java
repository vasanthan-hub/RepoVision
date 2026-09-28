package com.repovision.commit.infrastructure.persistence;

import com.repovision.commit.application.CommitStore;
import com.repovision.commit.domain.Commit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JpaCommitStore implements CommitStore {

    private final CommitJpaRepository commitJpaRepository;
    private final CommitMapper commitMapper;

    public JpaCommitStore(
            CommitJpaRepository commitJpaRepository,
            CommitMapper commitMapper
    ) {
        this.commitJpaRepository = commitJpaRepository;
        this.commitMapper = commitMapper;
    }

    @Override
    public List<Commit> saveAll(List<Commit> commits) {

        if (commits.isEmpty()) {
            return List.of();
        }

        UUID repositoryId =
                commits.get(0).getRepositoryId();

        List<String> hashes =
                commits.stream()
                        .map(Commit::getHash)
                        .toList();

        List<CommitEntity> existingEntities =
                commitJpaRepository
                        .findByRepositoryIdAndHashIn(
                                repositoryId,
                                hashes
                        );

        Map<String, CommitEntity> existingByHash =
                existingEntities.stream()
                        .collect(
                                Collectors.toMap(
                                        CommitEntity::getHash,
                                        Function.identity()
                                )
                        );

        List<CommitEntity> entitiesToSave =
                new ArrayList<>();

        for (Commit commit : commits) {

            CommitEntity existingEntity =
                    existingByHash.get(commit.getHash());

            if (existingEntity == null) {

                entitiesToSave.add(
                        commitMapper.toEntity(commit)
                );

            } else {

                existingEntity.setAuthorName(
                        commit.getAuthorName()
                );

                existingEntity.setAuthorEmail(
                        commit.getAuthorEmail()
                );

                existingEntity.setAuthoredAt(
                        commit.getAuthoredAt()
                );

                existingEntity.setSubject(
                        commit.getSubject()
                );

                existingEntity.setAdditions(
                        commit.getAdditions()
                );

                existingEntity.setDeletions(
                        commit.getDeletions()
                );

                entitiesToSave.add(existingEntity);
            }
        }

        List<CommitEntity> savedEntities =
                commitJpaRepository.saveAll(
                        entitiesToSave
                );

        return savedEntities.stream()
                .map(commitMapper::toDomain)
                .toList();
    }

    @Override
    public long countByRepositoryId(UUID repositoryId) {
        return commitJpaRepository.countByRepositoryId(repositoryId);
    }

    @Override
    public List<Commit> findByRepositoryId(UUID repositoryId) {
        return commitJpaRepository
                .findByRepositoryId(repositoryId)
                .stream()
                .map(commitMapper::toDomain)
                .toList();
    }
}