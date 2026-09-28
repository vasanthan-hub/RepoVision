package com.repovision.commit.application;

import com.repovision.commit.api.dto.CommitResponse;
import com.repovision.commit.infrastructure.persistence.CommitJpaRepository;
import com.repovision.commit.infrastructure.persistence.FileChangeJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class CommitIngestionIntegrationTest {

    @Autowired
    private CommitService commitService;

    @Autowired
    private CommitJpaRepository commitJpaRepository;

    @Autowired
    private FileChangeJpaRepository fileChangeJpaRepository;

    private static final UUID ENTERPRISE_PLATFORM_ID =
            UUID.fromString("9d13d996-144a-4cb7-a3f0-ebbd0c616221");

    @Test
    void shouldIngestCommitsAndFileChangesIdempotently() {
        List<CommitResponse> firstRun = commitService.readCommits(ENTERPRISE_PLATFORM_ID);
        assertFalse(firstRun.isEmpty());
        assertEquals(14, firstRun.size());

        long initialCommitCount = commitJpaRepository.countByRepositoryId(ENTERPRISE_PLATFORM_ID);
        assertEquals(14, initialCommitCount);

        long initialFileChangeCount = fileChangeJpaRepository.countByRepositoryId(ENTERPRISE_PLATFORM_ID);
        assertEquals(130, initialFileChangeCount);

        List<CommitResponse> secondRun = commitService.readCommits(ENTERPRISE_PLATFORM_ID);
        assertEquals(14, secondRun.size());

        long afterCommitCount = commitJpaRepository.countByRepositoryId(ENTERPRISE_PLATFORM_ID);
        assertEquals(14, afterCommitCount);

        long afterFileChangeCount = fileChangeJpaRepository.countByRepositoryId(ENTERPRISE_PLATFORM_ID);
        assertEquals(130, afterFileChangeCount);
    }
}
