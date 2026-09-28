package com.repovision.analysis.api;

import com.repovision.analysis.application.CommitAnalyticsService;
import com.repovision.analysis.api.dto.CommitAnalyticsResponse;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
public class CommitAnalyticsController {

    private final CommitAnalyticsService commitAnalyticsService;

    public CommitAnalyticsController(
            CommitAnalyticsService commitAnalyticsService
    ) {
        this.commitAnalyticsService = commitAnalyticsService;
    }

    @GetMapping("/{repositoryId}/analytics/commits")
    public CommitAnalyticsResponse getTotalCommits(
            @PathVariable UUID repositoryId
    ) {
        return commitAnalyticsService.getCommitAnalytics(
                repositoryId
        );
    }
}