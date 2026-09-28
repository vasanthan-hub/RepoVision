package com.repovision.report;

import com.repovision.analysis.api.dto.DeveloperAnalyticsResponse;
import com.repovision.analysis.application.DeveloperAnalyticsService;
import com.repovision.analysis.application.FileAnalyticsService;
import com.repovision.commit.application.CommitStore;
import com.repovision.commit.domain.Commit;
import com.repovision.intelligence.health.RepositoryHealthResponse;
import com.repovision.intelligence.health.RepositoryHealthService;
import com.repovision.intelligence.insight.InsightEngineService;
import com.repovision.intelligence.insight.InsightReportResponse;
import com.repovision.intelligence.risk.RiskEngineService;
import com.repovision.intelligence.risk.RiskReportResponse;
import com.repovision.repository.application.RepositoryService;
import com.repovision.repository.api.dto.RepositoryResponse;
import com.repovision.repository.domain.AnalysisStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ReportServiceTest {

    private RepositoryService repositoryService;
    private CommitStore commitStore;
    private FileAnalyticsService fileAnalyticsService;
    private DeveloperAnalyticsService developerAnalyticsService;
    private RiskEngineService riskEngineService;
    private RepositoryHealthService repositoryHealthService;
    private InsightEngineService insightEngineService;
    private ReportService reportService;
    private final UUID repoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        repositoryService = Mockito.mock(RepositoryService.class);
        commitStore = Mockito.mock(CommitStore.class);
        fileAnalyticsService = Mockito.mock(FileAnalyticsService.class);
        developerAnalyticsService = Mockito.mock(DeveloperAnalyticsService.class);
        riskEngineService = Mockito.mock(RiskEngineService.class);
        repositoryHealthService = Mockito.mock(RepositoryHealthService.class);
        insightEngineService = Mockito.mock(InsightEngineService.class);

        reportService = new ReportService(
                repositoryService,
                commitStore,
                fileAnalyticsService,
                developerAnalyticsService,
                riskEngineService,
                repositoryHealthService,
                insightEngineService
        );
    }

    @Test
    void shouldGenerateValidCommitsCsv() {
        Commit c = new Commit(UUID.randomUUID(), repoId, "hash123", "Alice", "alice@test.com", Instant.now(), "init", 50, 10);
        when(commitStore.findByRepositoryId(repoId)).thenReturn(List.of(c));

        byte[] csv = reportService.exportCommitsCsv(repoId);
        assertNotNull(csv);
        String text = new String(csv);
        assertTrue(text.contains("Hash,Author Name,Author Email"));
        assertTrue(text.contains("hash123"));
        assertTrue(text.contains("Alice"));
    }

    @Test
    void shouldGenerateValidHealthPdf() {
        RepositoryResponse repoResponse = new RepositoryResponse(
                repoId, "TestRepo", "/local/path", "main", AnalysisStatus.COMPLETED, Instant.now(), Instant.now()
        );
        when(repositoryService.getRepository(repoId)).thenReturn(repoResponse);
        when(commitStore.findByRepositoryId(repoId)).thenReturn(List.of());
        when(fileAnalyticsService.computeFileMetrics(repoId)).thenReturn(List.of());
        when(fileAnalyticsService.getHotspots(repoId, 5)).thenReturn(List.of());
        when(riskEngineService.analyzeRisks(repoId)).thenReturn(new RiskReportResponse(0, 0, 0, 0, List.of()));
        when(insightEngineService.generateInsights(repoId)).thenReturn(new InsightReportResponse(0, List.of()));
        when(repositoryHealthService.calculateHealth(repoId)).thenReturn(
                new RepositoryHealthResponse(85, "EXCELLENT", 85, 85, 85, 85, 85, List.of(), "Healthy repo")
        );

        byte[] pdf = reportService.exportHealthPdf(repoId);
        assertNotNull(pdf);
        assertTrue(pdf.length > 500);
        // PDF magic bytes %PDF-
        assertEquals('%', (char) pdf[0]);
        assertEquals('P', (char) pdf[1]);
        assertEquals('D', (char) pdf[2]);
        assertEquals('F', (char) pdf[3]);
    }
}
