package com.repovision.intelligence.risk;

import com.repovision.analysis.api.dto.FileMetrics;
import com.repovision.analysis.application.ChangeCouplingService;
import com.repovision.analysis.application.FileAnalyticsService;
import com.repovision.analysis.application.OwnershipAnalyticsService;
import com.repovision.branch.application.BranchAnalyticsService;
import com.repovision.commit.application.CommitStore;
import com.repovision.commit.domain.Commit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class RiskEngineServiceTest {

    private FileAnalyticsService fileAnalyticsService;
    private OwnershipAnalyticsService ownershipAnalyticsService;
    private BranchAnalyticsService branchAnalyticsService;
    private CommitStore commitStore;
    private ChangeCouplingService changeCouplingService;
    private RiskEngineService riskEngineService;
    private final UUID repoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        fileAnalyticsService = Mockito.mock(FileAnalyticsService.class);
        ownershipAnalyticsService = Mockito.mock(OwnershipAnalyticsService.class);
        branchAnalyticsService = Mockito.mock(BranchAnalyticsService.class);
        commitStore = Mockito.mock(CommitStore.class);
        changeCouplingService = Mockito.mock(ChangeCouplingService.class);

        riskEngineService = new RiskEngineService(
                fileAnalyticsService,
                ownershipAnalyticsService,
                branchAnalyticsService,
                commitStore,
                changeCouplingService
        );
    }

    @Test
    void shouldDetectHotspotAndLargeCommitRisks() {
        FileMetrics hotspotFile = new FileMetrics(
                "CriticalService.java",
                15, 800, 200, 1000, 1,
                Instant.now().minusSeconds(86400 * 10), Instant.now(),
                66.7, 85.0
        );

        when(fileAnalyticsService.computeFileMetrics(repoId)).thenReturn(List.of(hotspotFile));
        when(ownershipAnalyticsService.computeFileOwnerships(repoId)).thenReturn(List.of());
        when(changeCouplingService.computeCoupledFiles(repoId)).thenReturn(List.of());

        Commit bigCommit = new Commit(
                UUID.randomUUID(), repoId, "abc123456789", "Alice", "alice@example.com",
                Instant.now(), "giant commit", 1200, 300
        );
        when(commitStore.findByRepositoryId(repoId)).thenReturn(List.of(bigCommit));

        RiskReportResponse report = riskEngineService.analyzeRisks(repoId);
        assertNotNull(report);
        assertTrue(report.totalRisks() >= 2);
        assertTrue(report.highSeverityCount() >= 1);

        boolean hasHotspotRisk = report.risks().stream().anyMatch(r -> r.type() == RiskType.HOTSPOT);
        boolean hasLargeCommitRisk = report.risks().stream().anyMatch(r -> r.type() == RiskType.LARGE_COMMIT);

        assertTrue(hasHotspotRisk);
        assertTrue(hasLargeCommitRisk);
    }
}
