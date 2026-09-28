package com.repovision.analysis.application;

import com.repovision.analysis.api.dto.BusFactorResponse;
import com.repovision.analysis.api.dto.FileOwnership;
import com.repovision.commit.application.CommitStore;
import com.repovision.commit.application.FileChangeStore;
import com.repovision.commit.domain.Commit;
import com.repovision.commit.domain.FileChange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class OwnershipAnalyticsServiceTest {

    private CommitStore commitStore;
    private FileChangeStore fileChangeStore;
    private OwnershipAnalyticsService ownershipAnalyticsService;
    private final UUID repoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        commitStore = Mockito.mock(CommitStore.class);
        fileChangeStore = Mockito.mock(FileChangeStore.class);
        ownershipAnalyticsService = new OwnershipAnalyticsService(commitStore, fileChangeStore);
    }

    @Test
    void shouldCalculateOwnershipAndBusFactor() {
        UUID c1 = UUID.randomUUID();
        UUID c2 = UUID.randomUUID();

        Commit commit1 = new Commit(c1, repoId, "h1", "Alice", "alice@example.com", Instant.now(), "feat1", 100, 10);
        Commit commit2 = new Commit(c2, repoId, "h2", "Bob", "bob@example.com", Instant.now(), "feat2", 20, 2);

        when(commitStore.findByRepositoryId(repoId)).thenReturn(List.of(commit1, commit2));

        FileChange fc1 = new FileChange(UUID.randomUUID(), c1, "src/core/Main.java", 100, 10);
        FileChange fc2 = new FileChange(UUID.randomUUID(), c2, "src/util/Helper.java", 20, 2);

        when(fileChangeStore.findByRepositoryId(repoId)).thenReturn(List.of(fc1, fc2));

        List<FileOwnership> ownerships = ownershipAnalyticsService.computeFileOwnerships(repoId);
        assertEquals(2, ownerships.size());

        FileOwnership fo1 = ownerships.get(0);
        assertEquals("Alice", fo1.primaryContributorName());
        assertEquals(100.0, fo1.primaryContributorPercentage());
        assertEquals("HIGH", fo1.concentrationLevel());

        BusFactorResponse busFactor = ownershipAnalyticsService.getBusFactor(repoId);
        assertNotNull(busFactor);
        assertEquals(2, busFactor.totalFiles());
        assertEquals(2, busFactor.singleContributorFilesCount());
        assertFalse(busFactor.evidence().isEmpty());
    }
}
