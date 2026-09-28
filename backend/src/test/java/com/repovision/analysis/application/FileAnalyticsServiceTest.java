package com.repovision.analysis.application;

import com.repovision.analysis.api.dto.FileAnalyticsResponse;
import com.repovision.analysis.api.dto.FileMetrics;
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

class FileAnalyticsServiceTest {

    private CommitStore commitStore;
    private FileChangeStore fileChangeStore;
    private FileAnalyticsService fileAnalyticsService;
    private final UUID repoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        commitStore = Mockito.mock(CommitStore.class);
        fileChangeStore = Mockito.mock(FileChangeStore.class);
        fileAnalyticsService = new FileAnalyticsService(commitStore, fileChangeStore);
    }

    @Test
    void shouldReturnEmptyWhenNoCommitsOrFiles() {
        when(commitStore.findByRepositoryId(repoId)).thenReturn(List.of());
        when(fileChangeStore.findByRepositoryId(repoId)).thenReturn(List.of());

        FileAnalyticsResponse response = fileAnalyticsService.getFileAnalytics(repoId);
        assertNotNull(response);
        assertEquals(0, response.totalFiles());
        assertTrue(response.files().isEmpty());
    }

    @Test
    void shouldComputeAccurateFileMetricsAndHotspots() {
        UUID c1Id = UUID.randomUUID();
        UUID c2Id = UUID.randomUUID();
        Instant t1 = Instant.now().minusSeconds(86400 * 2);
        Instant t2 = Instant.now().minusSeconds(86400);

        Commit c1 = new Commit(c1Id, repoId, "hash1", "Alice", "alice@example.com", t1, "init", 100, 10);
        Commit c2 = new Commit(c2Id, repoId, "hash2", "Bob", "bob@example.com", t2, "update", 50, 5);

        when(commitStore.findByRepositoryId(repoId)).thenReturn(List.of(c1, c2));

        FileChange fc1 = new FileChange(UUID.randomUUID(), c1Id, "Service.java", 100, 10);
        FileChange fc2 = new FileChange(UUID.randomUUID(), c2Id, "Service.java", 50, 5);
        FileChange fc3 = new FileChange(UUID.randomUUID(), c1Id, "Readme.md", 10, 0);

        when(fileChangeStore.findByRepositoryId(repoId)).thenReturn(List.of(fc1, fc2, fc3));

        List<FileMetrics> hotspots = fileAnalyticsService.getHotspots(repoId, 10);
        assertEquals(2, hotspots.size());

        FileMetrics topHotspot = hotspots.get(0);
        assertEquals("Service.java", topHotspot.filePath());
        assertEquals(2, topHotspot.modificationCount());
        assertEquals(150, topHotspot.additions());
        assertEquals(15, topHotspot.deletions());
        assertEquals(165, topHotspot.churn());
        assertEquals(2, topHotspot.contributorCount());
        assertTrue(topHotspot.hotspotScore() > 0.0);

        FileMetrics second = hotspots.get(1);
        assertEquals("Readme.md", second.filePath());
        assertEquals(1, second.modificationCount());
        assertTrue(topHotspot.hotspotScore() > second.hotspotScore());
    }
}
