package com.repovision.analysis.application;

import com.repovision.analysis.api.dto.PeriodComparisonResponse;
import com.repovision.analysis.api.dto.WhatChangedResponse;
import com.repovision.commit.application.CommitStore;
import com.repovision.commit.application.FileChangeStore;
import com.repovision.commit.domain.Commit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ComparisonServiceTest {

    private CommitStore commitStore;
    private FileChangeStore fileChangeStore;
    private ComparisonService comparisonService;
    private final UUID repoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        commitStore = Mockito.mock(CommitStore.class);
        fileChangeStore = Mockito.mock(FileChangeStore.class);
        comparisonService = new ComparisonService(commitStore, fileChangeStore);
    }

    @Test
    void shouldHandleEmptyCommitsGracefully() {
        when(commitStore.findByRepositoryId(repoId)).thenReturn(List.of());

        PeriodComparisonResponse comparison = comparisonService.comparePeriods(repoId, 30);
        assertNotNull(comparison);
        assertEquals(30, comparison.days());
        assertTrue(comparison.metrics().isEmpty());

        WhatChangedResponse whatChanged = comparisonService.getWhatChanged(repoId, 30);
        assertNotNull(whatChanged);
        assertFalse(whatChanged.highlights().isEmpty());
    }

    @Test
    void shouldComputePeriodDeltasWithoutDivisionByZero() {
        Instant now = Instant.now();
        Commit c1 = new Commit(UUID.randomUUID(), repoId, "h1", "Alice", "alice@example.com", now.minusSeconds(86400 * 5), "recent", 100, 20);
        Commit c2 = new Commit(UUID.randomUUID(), repoId, "h2", "Bob", "bob@example.com", now.minusSeconds(86400 * 35), "older", 50, 10);

        when(commitStore.findByRepositoryId(repoId)).thenReturn(List.of(c1, c2));
        when(fileChangeStore.findByRepositoryId(repoId)).thenReturn(List.of());

        PeriodComparisonResponse comparison = comparisonService.comparePeriods(repoId, 30);
        assertNotNull(comparison);
        assertTrue(comparison.metrics().containsKey("commits"));
        assertTrue(comparison.metrics().containsKey("churn"));

        WhatChangedResponse whatChanged = comparisonService.getWhatChanged(repoId, 30);
        assertNotNull(whatChanged);
        assertFalse(whatChanged.detailedChanges().isEmpty());
    }
}
