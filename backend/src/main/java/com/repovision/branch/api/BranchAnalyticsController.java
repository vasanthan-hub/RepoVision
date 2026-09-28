package com.repovision.branch.api;

import com.repovision.branch.api.dto.BranchAnalyticsResponse;
import com.repovision.branch.application.BranchAnalyticsService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
public class BranchAnalyticsController {

    private final BranchAnalyticsService branchAnalyticsService;

    public BranchAnalyticsController(BranchAnalyticsService branchAnalyticsService) {
        this.branchAnalyticsService = branchAnalyticsService;
    }

    @GetMapping("/{repositoryId}/analytics/branches")
    public BranchAnalyticsResponse getBranchAnalytics(
            @PathVariable UUID repositoryId,
            @RequestParam(required = false, defaultValue = "30") long staleThresholdDays
    ) {
        return branchAnalyticsService.getBranchAnalytics(repositoryId, staleThresholdDays);
    }
}
