package com.repovision.analysis.api;

import com.repovision.analysis.api.dto.FileAnalyticsResponse;
import com.repovision.analysis.api.dto.FileDetailResponse;
import com.repovision.analysis.api.dto.FileMetrics;
import com.repovision.analysis.application.FileAnalyticsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
public class FileAnalyticsController {

    private final FileAnalyticsService fileAnalyticsService;

    public FileAnalyticsController(FileAnalyticsService fileAnalyticsService) {
        this.fileAnalyticsService = fileAnalyticsService;
    }

    @GetMapping("/{repositoryId}/analytics/files")
    public FileAnalyticsResponse getFileAnalytics(@PathVariable UUID repositoryId) {
        return fileAnalyticsService.getFileAnalytics(repositoryId);
    }

    @GetMapping("/{repositoryId}/analytics/files/hotspots")
    public List<FileMetrics> getHotspots(
            @PathVariable UUID repositoryId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return fileAnalyticsService.getHotspots(repositoryId, limit);
    }

    @GetMapping("/{repositoryId}/analytics/files/detail")
    public FileDetailResponse getFileDetail(
            @PathVariable UUID repositoryId,
            @RequestParam String path
    ) {
        return fileAnalyticsService.getFileDetail(repositoryId, path);
    }
}
