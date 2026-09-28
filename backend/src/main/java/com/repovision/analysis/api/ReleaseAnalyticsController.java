package com.repovision.analysis.api;

import com.repovision.analysis.api.dto.ReleaseAnalyticsResponse;
import com.repovision.analysis.application.ReleaseAnalyticsService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
public class ReleaseAnalyticsController {

    private final ReleaseAnalyticsService releaseAnalyticsService;

    public ReleaseAnalyticsController(ReleaseAnalyticsService releaseAnalyticsService) {
        this.releaseAnalyticsService = releaseAnalyticsService;
    }

    @GetMapping("/{repositoryId}/analytics/releases")
    public ReleaseAnalyticsResponse getReleaseAnalytics(@PathVariable UUID repositoryId) {
        return releaseAnalyticsService.getReleaseAnalytics(repositoryId);
    }
}
