package com.repovision.repository.infrastructure.persistence;

import com.repovision.repository.application.RepositoryStore;
import com.repovision.repository.domain.Repository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class JpaRepositoryStore implements RepositoryStore {

    private final RepositoryJpaRepository repositoryJpaRepository;
    private final RepositoryMapper repositoryMapper;

    public JpaRepositoryStore(
            RepositoryJpaRepository repositoryJpaRepository,
            RepositoryMapper repositoryMapper
    ) {
        this.repositoryJpaRepository = repositoryJpaRepository;
        this.repositoryMapper = repositoryMapper;
    }

    @Override
    public Repository save(Repository repository) {

        RepositoryEntity entity =
                repositoryMapper.toEntity(repository);

        RepositoryEntity savedEntity =
                repositoryJpaRepository.save(entity);

        return repositoryMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Repository> findById(UUID id) {

        return repositoryJpaRepository
                .findById(id)
                .map(repositoryMapper::toDomain);
    }

    @Override
    public java.util.List<Repository> findAll() {
        return repositoryJpaRepository.findAll()
                .stream()
                .map(repositoryMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        repositoryJpaRepository.deleteById(id);
    }
}