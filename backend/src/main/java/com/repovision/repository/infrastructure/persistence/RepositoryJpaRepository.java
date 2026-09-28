package com.repovision.repository.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RepositoryJpaRepository
        extends JpaRepository<RepositoryEntity, UUID> {
}