package com.repovision.analysis.api;

import com.repovision.analysis.api.dto.DeveloperAnalyticsResponse;
import com.repovision.analysis.application.DeveloperAnalyticsService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
public class DeveloperAnalyticsController {

    private final DeveloperAnalyticsService developerAnalyticsService;

    public DeveloperAnalyticsController(
            DeveloperAnalyticsService developerAnalyticsService
    ) {
        this.developerAnalyticsService =
                developerAnalyticsService;
    }

    @GetMapping("/{repositoryId}/analytics/developers")
    public DeveloperAnalyticsResponse getDeveloperAnalytics(
            @PathVariable UUID repositoryId
    ) {
        return developerAnalyticsService.getDeveloperAnalytics(
                repositoryId
        );
    }
}