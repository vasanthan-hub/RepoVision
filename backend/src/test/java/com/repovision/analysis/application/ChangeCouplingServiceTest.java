package com.repovision.analysis.application;

import com.repovision.analysis.api.dto.ChangeCouplingResponse;
import com.repovision.analysis.api.dto.FileCoupling;
import com.repovision.commit.application.FileChangeStore;
import com.repovision.commit.domain.FileChange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ChangeCouplingServiceTest {

    private FileChangeStore fileChangeStore;
    private ChangeCouplingService changeCouplingService;
    private final UUID repoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        fileChangeStore = Mockito.mock(FileChangeStore.class);
        changeCouplingService = new ChangeCouplingService(fileChangeStore);
    }

    @Test
    void shouldReturnEmptyWhenNoChanges() {
        when(fileChangeStore.findByRepositoryId(repoId)).thenReturn(List.of());
        ChangeCouplingResponse response = changeCouplingService.getChangeCoupling(repoId);
        assertNotNull(response);
        assertEquals(0, response.totalCoupledPairs());
    }

    @Test
    void shouldDetectCoupledFiles() {
        UUID c1 = UUID.randomUUID();
        UUID c2 = UUID.randomUUID();

        // Commit 1 changes A and B
        FileChange fc1 = new FileChange(UUID.randomUUID(), c1, "FileA.java", 10, 0);
        FileChange fc2 = new FileChange(UUID.randomUUID(), c1, "FileB.java", 10, 0);

        // Commit 2 changes A and B
        FileChange fc3 = new FileChange(UUID.randomUUID(), c2, "FileA.java", 5, 0);
        FileChange fc4 = new FileChange(UUID.randomUUID(), c2, "FileB.java", 5, 0);

        when(fileChangeStore.findByRepositoryId(repoId)).thenReturn(List.of(fc1, fc2, fc3, fc4));

        List<FileCoupling> couplings = changeCouplingService.computeCoupledFiles(repoId);
        assertEquals(1, couplings.size());

        FileCoupling coupling = couplings.get(0);
        assertEquals("FileA.java", coupling.fileA());
        assertEquals("FileB.java", coupling.fileB());
        assertEquals(2, coupling.coChangeCount());
        assertEquals(100.0, coupling.couplingPercentage());
        assertEquals("STRONG", coupling.strength());
    }
}
