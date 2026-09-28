package com.repovision.intelligence.health;

import com.repovision.analysis.api.dto.BusFactorResponse;
import com.repovision.analysis.api.dto.FileMetrics;
import com.repovision.analysis.application.FileAnalyticsService;
import com.repovision.analysis.application.OwnershipAnalyticsService;
import com.repovision.branch.application.BranchAnalyticsService;
import com.repovision.commit.application.CommitStore;
import com.repovision.commit.domain.Commit;
import com.repovision.git.GitClient;
import com.repovision.git.GitCommandResult;
import com.repovision.repository.application.RepositoryStore;
import com.repovision.repository.domain.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RepositoryHealthServiceTest {

    private CommitStore commitStore;
    private FileAnalyticsService fileAnalyticsService;
    private OwnershipAnalyticsService ownershipAnalyticsService;
    private BranchAnalyticsService branchAnalyticsService;
    private RepositoryStore repositoryStore;
    private GitClient gitClient;
    private RepositoryHealthService healthService;
    private final UUID repoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        commitStore = Mockito.mock(CommitStore.class);
        fileAnalyticsService = Mockito.mock(FileAnalyticsService.class);
        ownershipAnalyticsService = Mockito.mock(OwnershipAnalyticsService.class);
        branchAnalyticsService = Mockito.mock(BranchAnalyticsService.class);
        repositoryStore = Mockito.mock(RepositoryStore.class);
        gitClient = Mockito.mock(GitClient.class);

        healthService = new RepositoryHealthService(
                commitStore,
                fileAnalyticsService,
                ownershipAnalyticsService,
                branchAnalyticsService,
                repositoryStore,
                gitClient
        );
    }

    @Test
    void shouldCalculateExplainableHealthScores() {
        Commit c1 = new Commit(UUID.randomUUID(), repoId, "h1", "Alice", "alice@example.com", Instant.now(), "feat", 50, 5);
        when(commitStore.findByRepositoryId(repoId)).thenReturn(List.of(c1));

        when(fileAnalyticsService.computeFileMetrics(repoId)).thenReturn(List.of());
        when(ownershipAnalyticsService.getBusFactor(repoId)).thenReturn(
                new BusFactorResponse(1, 1, 1, 1, List.of(), List.of())
        );
        when(branchAnalyticsService.getBranchAnalytics(repoId)).thenReturn(
                new com.repovision.branch.api.dto.BranchAnalyticsResponse(1, 1, 0, List.of())
        );

        Repository repo = new Repository(repoId, "Demo", Path.of("/test"), "main");
        when(repositoryStore.findById(repoId)).thenReturn(Optional.of(repo));
        when(gitClient.execute(any(), any())).thenReturn(new GitCommandResult(0, "", ""));

        RepositoryHealthResponse response = healthService.calculateHealth(repoId);
        assertNotNull(response);
        assertTrue(response.overallScore() >= 0 && response.overallScore() <= 100);
        assertEquals(5, response.dimensions().size());
        assertNotNull(response.status());
        assertNotNull(response.summary());
    }
}
