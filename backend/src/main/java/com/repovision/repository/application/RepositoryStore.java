package com.repovision.repository.application;

import com.repovision.repository.domain.Repository;

import java.util.Optional;
import java.util.UUID;

public interface RepositoryStore {

    Repository save(Repository repository);

    Optional<Repository> findById(UUID id);

    java.util.List<Repository> findAll();

    void deleteById(UUID id);
}